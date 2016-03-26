library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

entity primesim_top is
end primesim_top;

architecture RTL of primesim_top is



  signal clk : std_logic := '0';
  signal reset : std_logic := '0';
  signal counter : signed(32-1 downto 0) := (others => '0');
  type Type_main is (
    main_IDLE,
    S0  
  );
  signal main : Type_main := main_IDLE;
  signal main_delay : signed(32-1 downto 0) := (others => '0');
  signal tmp_0001 : signed(32-1 downto 0);
  signal tmp_0002 : std_logic;
  signal tmp_0003 : std_logic;
  signal tmp_0004 : std_logic;
  signal tmp_0005 : std_logic;

  component PrimeSim
    port (
      clk : in std_logic;
      reset : in std_logic;
      finish_flag_out : out std_logic;
      finish_flag_in : in std_logic;
      finish_flag_we : in std_logic;
      result_in : in signed(32-1 downto 0);
      result_we : in std_logic;
      result_out : out signed(32-1 downto 0);
      run_req : in std_logic;
      run_busy : out std_logic;
      start_req : in std_logic;
      start_busy : out std_logic;
      join_req : in std_logic;
      join_busy : out std_logic;
      yield_req : in std_logic;
      yield_busy : out std_logic
      );
  end component primeSim;

  signal finish_flag : std_logic := '0';

begin

  U: PrimeSim port map(
    clk => clk,
    reset => reset,
    finish_flag_out => finish_flag,
    finish_flag_in => '0',
    finish_flag_we => '0',
    result_in => (others => '0'),
    result_we => '0',
    result_out => open,
    run_req => '1',
    run_busy => open,
    start_req => '0',
    start_busy => open,
    join_req => '0',
    join_busy => open,
    yield_req => '0',
    yield_busy => open
    );
  
  tmp_0001 <= counter + 1;
  tmp_0002 <= '1' when counter > 3 else '0';
  tmp_0003 <= '1' when counter < 8 else '0';
  tmp_0004 <= tmp_0002 and tmp_0003;
  tmp_0005 <= '1' when tmp_0004 = '1' else '0';

  process
  begin
    -- state main = main_IDLE
    main <= S0;
    wait for 10 ns;
    -- state main = S0
    main <= main_IDLE;
    wait for 10 ns;
  end process;


  process(main)
  begin
    if main = main_IDLE then
      clk <= '0';
    elsif main = S0 then
      clk <= '1';
    end if;
  end process;

  process(main)
  begin
    reset <= tmp_0005;
  end process;

  process(main)
  begin
    if main = main_IDLE then
      counter <= tmp_0001;
    elsif main = S0 then
      counter <= tmp_0001;
    end if;
  end process;

  process(clk)
  begin
   if clk'event and clk = '1' then
     if finish_flag = '1' then
       assert (false) report "Simulation End!" severity failure;
     end if;
  end if;
  end process;

end RTL;
