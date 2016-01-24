library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

entity sim is
end sim;

architecture RTL of sim is

  signal clk : std_logic := '0';
  signal reset : std_logic := '0';
  signal counter : signed(32-1 downto 0) := (others => '0');
  type Type_main is (
    main_IDLE,
    main_S0  
  );
  signal main : Type_main := main_IDLE;
  signal main_delay : signed(32-1 downto 0) := (others => '0');
  signal tmp_0001 : signed(32-1 downto 0);
  signal tmp_0002 : std_logic;
  signal tmp_0003 : std_logic;
  signal tmp_0004 : std_logic;
  signal tmp_0005 : std_logic;

 component Test005
  port (
    clk : in std_logic;
    reset : in std_logic;
    run_req : in std_logic;
    run_busy : out std_logic
  );
 end component Test005;

 component Test001Sim
  port (
    clk : in std_logic;
    reset : in std_logic;
    main_req : in std_logic;
    main_busy : out std_logic
  );
 end component Test001Sim;

 component Test003
  port (
    clk : in std_logic;
    reset : in std_logic;
    test_return : out std_logic;
    test_req : in std_logic;
    test_busy : out std_logic
  );
 end component Test003;

 component Test006
  port (
    clk : in std_logic;
    reset : in std_logic;
    test_req : in std_logic;
    test_busy : out std_logic
  );
 end component Test006;

 component Test009
  port (
    clk : in std_logic;
    reset : in std_logic;
    test_req : in std_logic;
    test_busy : out std_logic
  );
 end component Test009;

 component Test004
  port (
    clk : in std_logic;
    reset : in std_logic;
    run_req : in std_logic;
    run_busy : out std_logic;
    break_test_req : in std_logic;
    break_test_busy : out std_logic;
    continue_test_req : in std_logic;
    continue_test_busy : out std_logic;
    continue_test_return : out signed(32-1 downto 0);
    test_return : out std_logic;
    test_busy : out std_logic;
    test_req : in std_logic;
    start_req : in std_logic;
    start_busy : out std_logic;
    join_req : in std_logic;
    join_busy : out std_logic;
    yield_req : in std_logic;
    yield_busy : out std_logic
  );
 end component Test004;

 signal run_req, run_busy: std_logic;

 signal req_001 : std_logic := '0';

begin


  tmp_0001 <= counter + 1;
  tmp_0002 <= '1' when counter > 3 else '0';
  tmp_0003 <= '1' when counter < 8 else '0';
  tmp_0004 <= tmp_0002 and tmp_0003;
  tmp_0005 <= '1' when tmp_0004 = '1' else '0';

  process
  begin
    -- state main = main_IDLE
    main <= main_S0;
    wait for 10 ns;
    -- state main = main_S0
    main <= main_IDLE;
    wait for 10 ns;
  end process;


  process(main)
  begin
    if main = main_IDLE then
      clk <= '0';
    elsif main = main_S0 then
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
    elsif main = main_S0 then
      counter <= tmp_0001;
    end if;
  end process;

 run_req <= '1' when counter = 100 else '0';
 U005: Test005
  port map(
    clk => clk,
    reset => reset,
    run_req => run_req,
    run_busy => run_busy
  );

 req_001 <= '1' when counter > 50 else '0';
  
 U001: Test001Sim
  port map(
    clk => clk,
    reset => reset,
    main_req => req_001,
    main_busy => open
  );

 U003: Test003
  port map(
    clk => clk,
    reset => reset,
    test_return => open,
    test_req => req_001,
    test_busy => open
  );

 U006: Test006
  port map(
    clk => clk,
    reset => reset,
    test_req => req_001,
    test_busy => open
  );

-- U009: Test009
--  port map(
--    clk => clk,
--    reset => reset,
--    test_req => req_001,
--    test_busy => open
--  );

 U004: Test004
  port map(
    clk => clk,
    reset => reset,
    run_req => '0',
    run_busy => open,
    break_test_req => '1',
    break_test_busy => open,
    continue_test_req => '1',
    continue_test_busy => open,
    continue_test_return => open,
    test_return => open,
    test_busy => open,
    test_req => '0',
    start_req => '1',
    start_busy => open,
    join_req => '0',
    join_busy => open,
    yield_req => '0',
    yield_busy => open
  );

end RTL;
