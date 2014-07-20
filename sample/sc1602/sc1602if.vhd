-- -*- coding: sjis-dos -*- --
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;

entity sc1602if is
  generic (
    CLKWAIT : integer := 16
    );
  port (
    pClk     : in  std_logic;
    pLCD_RS  : out std_logic;
    pLCD_E   : out std_logic;
    pLCD_DB  : out std_logic_vector(3 downto 0);
    pLCD_RW  : out std_logic;
    pLED  : out std_logic_vector(3 downto 0);

    pReq  : in  std_logic;
    pBusy : out std_logic;
    pWrData : in std_logic_vector(7 downto 0);
    pWrAddr : in std_logic_vector(6 downto 0);
    pWrWe   : in std_logic_vector(0 downto 0);
    
    pReset  : in  std_logic
  );
end sc1602if;

architecture RTL of sc1602if is

  component sc1602
    port (
      pClk   : in std_logic;              -- 65us���炢
      pLCD_E : out std_logic;
      pLCD_RS : out std_logic;
      pLCD_DB : out std_logic_vector(3 downto 0);
      pReq  : in  std_logic;
      pBusy : out std_logic;
      pWrClk  : in std_logic;
      pWrData : in std_logic_vector(7 downto 0);
      pWrAddr : in std_logic_vector(6 downto 0);
      pWrWe   : in std_logic_vector(0 downto 0);
      pReset : in std_logic
      );
  end component;

  signal cBusy         : std_logic;
  signal cReq          : std_logic;
  signal iStateCounter : std_logic_vector(2 downto 0);
  signal iSubStateCounter : std_logic_vector(1 downto 0);

  signal cWrClk  : std_logic;
  signal cWrData : std_logic_vector(7 downto 0);
  signal cWrAddr : std_logic_vector(6 downto 0);
  signal cWrWe   : std_logic_vector(0 downto 0);

  signal cU_SC1602_Busy  : std_logic;
  signal cU_SC1602_Req  : std_logic;
  signal cU_SC1602_WrData : std_logic_vector(7 downto 0);
  signal cU_SC1602_WrAddr : std_logic_vector(6 downto 0);
  signal cU_SC1602_WrWe   : std_logic_vector(0 downto 0);

  signal clkcounter : std_logic_vector(CLKWAIT downto 0);

begin

  pLCD_RW <= '0';

  U_SC1602: sc1602 port map(
    pClk     => clkcounter(CLKWAIT),
    pLCD_RS  => pLCD_RS,
    pLCD_E   => pLCD_E,
    pLCD_DB  => pLCD_DB,
    pReq     => cU_SC1602_Req,
    pBusy    => cU_SC1602_Busy,
    pWrClk   => cWrClk,
    pWrData  => cU_SC1602_WrData,
    pWrAddr  => cU_SC1602_WrAddr,
    pWrWe    => cU_SC1602_WrWe,
    pReset   => pReset
    );

  cBusy <= cU_SC1602_Busy;
  cU_SC1602_Req <= cReq;

  process (cU_SC1602_Busy, iStateCounter)
  begin
    if conv_integer(iStateCounter) >= 5 then
      pBusy <= cU_SC1602_Busy or cReq or pReq;
    else
      pBusy <= '1';
    end if;
  end process;

  process (cWrWe, pWrWe, iStateCounter)
  begin
    if conv_integer(iStateCounter) >= 5 then
      cU_SC1602_WrWe <= pWrWe;
    else
      cU_SC1602_WrWe <= cWrWe;
    end if;
  end process;

  process (cWrAddr, pWrAddr, iStateCounter)
  begin
    if conv_integer(iStateCounter) >= 5 then
      cU_SC1602_WrAddr <= pWrAddr;
    else
      cU_SC1602_WrAddr <= cWrAddr;
    end if;
  end process;

  process (cWrData, pWrData, iStateCounter)
  begin
    if conv_integer(iStateCounter) >= 5 then
      cU_SC1602_WrData <= pWrData;
    else
      cU_SC1602_WrData <= cWrData;
    end if;
  end process;

  pLED(0) <= not pReset;
  pLED(1) <= cBusy;
  pLED(2) <= '0';
  pLED(3) <= '1';

  cWrClk <= pClk;

  process (pClk, pReset)
  begin  -- process
    if pReset = '1' then                -- asynchronous reset (active high)
      iStateCounter <= (others => '0');
      iSubStateCounter <= (others => '0');
      cWrWe <= (others => '0');
      cWrAddr <= (others => '0');
      cWrData <= (others => '0');
      cReq <= '0';
      clkcounter <= (others => '0');
    elsif pClk'event and pClk = '1' then  -- rising clock edge
      clkcounter <= clkcounter + 1;
      case conv_integer(iStateCounter) is
        -----------------------------------------------------------
        -- �������̏�����
        -----------------------------------------------------------
        when 0 =>
          case conv_integer(iSubStateCounter) is
            when 0 =>                   -- ��������
              cWrData <= X"20";
              cWrWe <= "1";
              iSubStateCounter <= iSubStateCounter + 1;
            when 1 =>                   -- �A�h���X
              cWrAddr <= cWrAddr + 1;
              cWrWe <= "0";
              iSubStateCounter <= (others => '0');
              if cWrAddr = "1111111" then  -- �������̏������I��
                iStateCounter <= iStateCounter + 1;
              end if;
            when others => null;
          end case;
        -----------------------------------------------------------
        -- LCD�̏�������҂�
        -----------------------------------------------------------
        when 1 =>
          if cBusy = '0' then
            iStateCounter <= iStateCounter + 1;
          end if;
        -----------------------------------------------------------
        -- LCD�Ƀ��N�G�X�g�𔭍s����
        -----------------------------------------------------------
        when 2 =>
          cReq <= '1';
          iStateCounter <= iStateCounter + 1;
        -----------------------------------------------------------
        -- LCD�Ƀ��N�G�X�g���󗝂����̂�҂�
        -----------------------------------------------------------
        when 3 =>
          if cBusy = '1' then
            iStateCounter <= iStateCounter + 1;
            cReq <= '0';
          end if;
        -----------------------------------------------------------
        -- LCD�̏������I���̂�҂�
        -----------------------------------------------------------
        when 4 =>
          if cBusy = '0' then
            iStateCounter <= iStateCounter + 1;
          end if;
        -----------------------------------------------------------
        -- LCD�Ƀ��N�G�X�g��ʒm����
        -----------------------------------------------------------
        when 5 =>
          if pReq = '1' then
            cReq <= '1';
            iStateCounter <= iStateCounter + 1;
          end if;
        -----------------------------------------------------------
        -- LCD�Ƀ��N�G�X�g���󗝂����̂�҂�
        -----------------------------------------------------------
        when 6 =>
          if cBusy = '1' then
            cReq <= '0';
            iStateCounter <= iStateCounter + 1;
          end if;
        -----------------------------------------------------------
        -- LCD�̏������I���̂�҂�
        -----------------------------------------------------------
        when 7 =>
          if cBusy = '0' then
            iStateCounter <= conv_std_logic_vector(5, 3);
          end if;
        when others =>
          null;
      end case;
    end if;
  end process;

end RTL;

