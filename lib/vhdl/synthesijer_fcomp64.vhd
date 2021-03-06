library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity synthesijer_fcomp64 is
  port (
    clk    : in  std_logic;
    reset  : in  std_logic;
    a      : in  std_logic_vector(64-1 downto 0);
    b      : in  std_logic_vector(64-1 downto 0);
    opcode : in  std_logic_vector(7 downto 0);
    nd     : in  std_logic;
    result : out std_logic;
    valid  : out std_logic
    );
end synthesijer_fcomp64;

architecture RTL of synthesijer_fcomp64 is

  component fcomp64_ip
    port (
      clock                    : in  std_logic;
      dataa          : in  std_logic_vector(64-1 downto 0);
      datab          : in  std_logic_vector(64-1 downto 0);
      aeb    : out std_logic
    );
  end component fcomp64_ip;

  signal result_tmp : std_logic_vector(7 downto 0);

begin

  U: fcomp64_ip port map(
    clock                 => clk,
    dataa       => a,
    datab       => b,
    aeb => valid
  );

  result <= result_tmp(0);

end RTL;
