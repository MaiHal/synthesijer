library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity synthesijer_fmul64 is
  port (
    clk    : in  std_logic;
    reset  : in  std_logic;
    a      : in  std_logic_vector(64-1 downto 0);
    b      : in  std_logic_vector(64-1 downto 0);
    nd     : in  std_logic;
    result : out std_logic_vector(64-1 downto 0);
    valid  : out std_logic
    );
end synthesijer_fmul64;

architecture RTL of synthesijer_fmul64 is

  component fmul64_ip
    port (
      aclk                 : in  std_logic;
      s_axis_a_tdata       : in  std_logic_vector(64-1 downto 0);
      s_axis_a_tvalid      : in  std_logic;
      s_axis_b_tdata       : in  std_logic_vector(64-1 downto 0);
      s_axis_b_tvalid      : in  std_logic;
      m_axis_result_tvalid : out std_logic;
      m_axis_result_tdata  : out std_logic_vector(64-1 downto 0)
      );
  end component fmul64_ip;

begin

  U: fmul64_ip port map(
    aclk                 => clk,
    s_axis_a_tdata       => a,
    s_axis_a_tvalid      => nd,
    s_axis_b_tdata       => b,
    s_axis_b_tvalid      => nd,
    m_axis_result_tvalid => valid,
    m_axis_result_tdata  => result
    );

end RTL;
