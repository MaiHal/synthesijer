package synthesijer.scala.vhdl

import org.scalatest._

class VHDLParserTest extends FlatSpec with Matchers {

  "identifiers" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.identifier, "ident").get should be ("ident")
    obj.parseAll(obj.identifier, "iDENT").get should be ("iDENT")
    obj.parseAll(obj.identifier, "ident0123").get should be ("ident0123")
    obj.parseAll(obj.identifier, "ident ").get should be ("ident")
  }

  "long names" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.long_name, "a").get should be ("a")
    obj.parseAll(obj.long_name, "Test000").get should be ("Test000")
    obj.parseAll(obj.long_name, "a.bb.cCcc").get should be ("a.bb.cCcc")
    obj.parseAll(obj.long_name, "ieee.std_logic_1164").get should be ("ieee.std_logic_1164")
  }

  "selected names" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.selected_name, "a.bb.cCcc.all").get should be ("a.bb.cCcc.all")
    obj.parseAll(obj.selected_name, "ieee.std_logic_1164.all").get should be ("ieee.std_logic_1164.all")
  }

  "use-statement" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.use_clause, "use ieee.std_logic_1164.all;").get should be (List(new Use("ieee.std_logic_1164.all")))
  }

  "a library (with lower-case)" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.library_clause, "library ieee;").get should be (List(new Library("ieee")))
  }

  "a library (with capital)" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.library_clause, "LIBRARY ieee;").get should be (List(new Library("ieee")))
  }

  "a library (with mixed-case)" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.library_clause, "lIBRary ieee;").get should be (List(new Library("ieee")))
  }

  "2 libraries" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.library_clause, "LIBRARY ieee, Work;").get should be (List(new Library("ieee"), new Library("Work")))
  }

  "3 libraries" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parseAll(obj.library_clause, "LIBRARY ieee, work, test0123;").get should be (List(new Library("ieee"), new Library("work"), new Library("test0123")))
  }

  "simple entity decl" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.entity_decl, "entity Test000 is end;")
      .get should be (
        new Entity("Test000", None)
      )
  }

  "entity decl" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.entity_decl, "entity Test000 is end Test000;")
      .get should be (
        new Entity("Test000", None)
      )
  }

  "full entity decl" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.entity_decl, "entity Test000 is end entity Test000;")
      .get should be (
        new Entity("Test000", None)
      )
  }

  "index_value" should "parses 3 as String(3)" in {
    val obj = new VHDLParser()
    obj.parse(obj.index_value, "3").get should be ("3")
  }

  "index_value" should "parses WIDTH as String(WIDTH)" in {
    val obj = new VHDLParser()
    obj.parse(obj.index_value, "WIDTH").get should be ("WIDTH")
  }

  "index_value" should "parses 3+5 as String(3+5)" in {
    val obj = new VHDLParser()
    obj.parse(obj.index_value, "3+5").get should be ("3+5")
  }

  "index_value" should "parses 32-1 as String(32-1)" in {
    val obj = new VHDLParser()
    obj.parse(obj.index_value, "32-1").get should be ("32-1")
  }

  "kind (std_logic)" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.kind, "std_logic")
      .get should be (new StdLogic())
  }

  "kind (std_logic_vector)" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.kind, "std_logic_vector(3 downto 0)")
      .get should be (new VectorKind("std_logic_vector", "downto", "3", "0"))
  }

  "kind (signed)" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.kind, "signed(3 downto 0)")
      .get should be (new VectorKind("signed", "downto", "3", "0"))
  }

  "kind (unsigned)" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.kind, "unsigned(3 downto 0)")
      .get should be (new VectorKind("unsigned", "downto", "3", "0"))
  }

  "port item" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.port_item, "clk : in std_logic")
      .get should be (new PortItem("clk", "in", new StdLogic()))
    obj.parse(obj.port_item, "q : out std_logic_vector(3 downto 0)")
      .get should be (new PortItem("q", "out", new VectorKind("std_logic_vector", "downto", "3", "0")))
  }

  "port item list" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.port_item_list, """
port (
clk   : in std_logic;
reset : in std_logic;
sw    : in std_logic_vector(3 downto 0);
q     : out std_logic
);
"""
    )
      .get should be (
        List(
          new PortItem("clk",   "in",  new StdLogic()),
          new PortItem("reset", "in",  new StdLogic()),
          new PortItem("sw",    "in",  new VectorKind("std_logic_vector", "downto", "3", "0")),
          new PortItem("q",     "out", new StdLogic())
        )
      )
  }

  "entity" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.entity_decl, """
entity Test000 is
  port (
    clk : in std_logic;
    reset : in std_logic;
    ic_in : in signed(32-1 downto 0);
    ic_we : in std_logic;
    ic_out : out signed(32-1 downto 0);
    lc_in : in signed(64-1 downto 0);
    lc_we : in std_logic;
    lc_out : out signed(64-1 downto 0);
    x_in : in signed(32-1 downto 0);
    x_we : in std_logic;
    x_out : out signed(32-1 downto 0);
    y_in : in signed(64-1 downto 0);
    y_we : in std_logic;
    y_out : out signed(64-1 downto 0);
    test_ia : in signed(32-1 downto 0);
    test_ib : in signed(32-1 downto 0);
    test_la : in signed(64-1 downto 0);
    test_lb : in signed(64-1 downto 0);
    test_busy : out std_logic;
    test_req : in std_logic
  );
end Test000;
"""
    )
      .get should be (
        new Entity("Test000",
          Some(List(
            new PortItem("clk",    "in",  new StdLogic()),
            new PortItem("reset",  "in",  new StdLogic()),
            new PortItem("ic_in",  "in",  new VectorKind("signed", "downto", "32-1", "0")),
            new PortItem("ic_we",  "in",  new StdLogic()),
            new PortItem("ic_out", "out", new VectorKind("signed", "downto", "32-1", "0")),
            new PortItem("lc_in",  "in",  new VectorKind("signed", "downto", "64-1", "0")),
            new PortItem("lc_we",  "in",  new StdLogic()),
            new PortItem("lc_out", "out", new VectorKind("signed", "downto", "64-1", "0")),
            new PortItem("x_in",  "in",  new VectorKind("signed", "downto", "32-1", "0")),
            new PortItem("x_we",  "in",  new StdLogic()),
            new PortItem("x_out", "out", new VectorKind("signed", "downto", "32-1", "0")),
            new PortItem("y_in",  "in",  new VectorKind("signed", "downto", "64-1", "0")),
            new PortItem("y_we",  "in",  new StdLogic()),
            new PortItem("y_out", "out", new VectorKind("signed", "downto", "64-1", "0")),
            new PortItem("test_ia", "in", new VectorKind("signed", "downto", "32-1", "0")),
            new PortItem("test_ib", "in", new VectorKind("signed", "downto", "32-1", "0")),
            new PortItem("test_la", "in", new VectorKind("signed", "downto", "64-1", "0")),
            new PortItem("test_lb", "in", new VectorKind("signed", "downto", "64-1", "0")),
            new PortItem("test_busy", "out", new StdLogic()),
            new PortItem("test_req", "in", new StdLogic())
          )
          )
        )
      )
  }

  "architecture_decl" should "be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.architecture_decl, """
architecture RTL of Test000 is
begin
end RTL;
""").get should be (new Architecture("RTL", "Test000", List(), List()))
  }

  "attribute_decl" should " be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.attribute_decl, "attribute mark_debug : string;").
      get should be (new Attribute("mark_debug", "string"))
  }

  "component_decl (null)" should " be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.component_decl, """
component null_component
end component null_component;
"""
    ).get should be(new ComponentDecl("null_component", None))
  }

  "component_decl" should " be parsed" in {
    val obj = new VHDLParser()
    obj.parse(obj.component_decl, """
component synthesijer_mul32
  port (
    clk : in std_logic;
    reset : in std_logic;
    a : in signed(32-1 downto 0);
    b : in signed(32-1 downto 0);
    nd : in std_logic;
    result : out signed(32-1 downto 0);
    valid : out std_logic
  );
end component synthesijer_mul32;
"""
    ).get should be(
      new ComponentDecl("synthesijer_mul32",
        Some(List(
          new PortItem("clk", "in", new StdLogic()),
          new PortItem("reset", "in", new StdLogic()),
          new PortItem("a", "in", new VectorKind("signed", "downto", "32-1", "0")),
          new PortItem("b", "in", new VectorKind("signed", "downto", "32-1", "0")),
          new PortItem("nd", "in", new StdLogic()),
          new PortItem("result", "out", new VectorKind("signed", "downto", "32-1", "0")),
          new PortItem("valid", "out", new StdLogic())
        ))
      )
    )
  }

  "signal decl (std_logic without init)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.signal_decl, """signal clk_sig : std_logic;""").
      get should be ( new Signal("clk_sig", new StdLogic(), None) )
  }

  "bit value" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.bit_value, "'0'").get should be ( "'0'" )
  }

  "init value" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.init_value, ":= '0'").get should be ( "'0'" )
  }

  "init value (others => '0')" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.init_value, ":= (others => '0')").get should be ( "(others=>'0')" )
  }
  
  "signal decl (std_logic with init)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.signal_decl, "signal clk_sig : std_logic := '0';").
      get should be ( new Signal("clk_sig", new StdLogic(), Some("'0'")) )
  }

  "signal decl (signed with init)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.signal_decl, "signal ic_in_sig : signed(32-1 downto 0) := (others => '0');").
      get should be ( new Signal("ic_in_sig", new VectorKind("signed", "downto", "32-1", "0"), Some("(others=>'0')")) )
  }

  "signal decl (std_logic_vector with init)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.signal_decl, "signal test_fc_0064 : std_logic_vector(32-1 downto 0) := (others => '0');").
      get should be ( new Signal("test_fc_0064", new VectorKind("std_logic_vector", "downto", "32-1", "0"), Some("(others=>'0')")) )
  }

  "symbol list" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.symbol_list,
      "test_method_IDLE, test_method_S_0000, test_method_S_0001").
      get should be ( List("test_method_IDLE", "test_method_S_0000", "test_method_S_0001"))
  }

  "type decl" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.type_decl,
      "type Type_test_method is (test_method_IDLE, test_method_S_0000, test_method_S_0001);").
      get should be ( new UserType("Type_test_method", List("test_method_IDLE", "test_method_S_0000", "test_method_S_0001")))
  }

  "signal decl (user defined type without init)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.signal_decl, "signal test_method : Type_test_method;").
      get should be ( new Signal("test_method", new UserTypeKind("Type_test_method"), None))
  }

  "signal decl (user defined type with init)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.signal_decl, "signal test_method : Type_test_method := test_method_IDLE;").
      get should be ( new Signal("test_method", new UserTypeKind("Type_test_method"), Some("test_method_IDLE")) )
  }

  "simple_expression signal" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.simple_expression, "clk").
      get should be ( "clk" )
  }

  "clk_sig <= clk;" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.assign_statement, "clk_sig <= clk;").
      get should be ( new AssignStatement("clk_sig", new Ident("clk")) )
  }

  "process, \"process begin end process;\"" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.process_statement, "process begin end process;").
      get should be ( new ProcessStatement(None, None, List()) )
  }

  "sensitivity_list \"()\"" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.sensitivity_list, "()").get should be (List())
  }

  "sensitivity_list \"(clk)\"" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.sensitivity_list, "(clk)").get should be (List("clk"))
  }

  "sensitivity_list \"(clk, reset)\"" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.sensitivity_list, "(clk, reset)").get should be (List("clk", "reset"))
  }

  "process \"process() begin end process;\"" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.process_statement, "process() begin end process;").
      get should be ( new ProcessStatement(Some(List()), None, List()) )
  }

  "process (empty, with label)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.process_statement, "LABEL: process() begin end process LABEL;").
      get should be ( new ProcessStatement(Some(List()), Some("LABEL"), List()) )
  }

  "process (with clk)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.process_statement, "process(clk) begin end process;").
      get should be ( new ProcessStatement(Some(List("clk")), None, List()) )
  }

  "process (with clk and reset)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.process_statement, "process(clk, reset) begin end process;").
      get should be ( new ProcessStatement(Some(List("clk", "reset")), None, List()) )
  }

  "process (with multiple)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.process_statement, "process(a, b, c) begin end process;").
      get should be ( new ProcessStatement(Some(List("a","b","c")), None, List()) )
  }

  "process (with clk and body)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.process_statement, """
process(clk)
begin
  if rising_edge(clk) then
  end if;
end process;
""").
      get should be (
        new ProcessStatement(
          Some(List("clk")),
          None,
          List(new IfStatement(
            new CallExpr("rising_edge", List("clk")),
            List(),
            List(),
            None
          )
          )
        )
      )
  }

  "expr '1'" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "'1'").get should be (new Constant("'1'"))
  }

  "expr clk" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "clk").get should be (new Ident("clk"))
  }

  "expr clk'event" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "clk'event").get should be (new Ident("clk'event"))
  }

  "expr clk = '1'" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "clk = '1'").get should be (new BinaryExpr("=", new Ident("clk"), new Constant("'1'")))
  }

  "expr clk'event and clk = '1'" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "clk'event and clk = '1'").
      get should be (new BinaryExpr("and", new Ident("clk'event"), new BinaryExpr("=", new Ident("clk"), new Constant("'1'"))))
  }

  "expr rising_edge(clk)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "rising_edge(clk)").get should be (new CallExpr("rising_edge", List("clk")))
  }

  "compare =" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.compare_operation, "=").get should be ( "=" )
  }

  "compare /=" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.compare_operation, "/=").get should be ( "/=" )
  }

  "when expr 1" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.when_expression, "ic_in_sig when ic_we_sig = '1' else class_ic_0000").
      get should be (
        new WhenExpr(
          new BinaryExpr("=", new Ident("ic_we_sig"), new Constant("'1'")), // cond
          new Ident("ic_in_sig"),    // then-expr
          new Ident("class_ic_0000") // else-expr
        )
      )
  }

  "when expr 2" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.when_expression, "'1' when test_method = test_method_S_0000 else '0'").
      get should be (
        new WhenExpr(
          new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0000")), // cond
          new Constant("'1'"),    // then-expr
          new Constant("'0'") // else-expr
        )
      )
  }

  "assignement with when" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.assign_statement, "tmp_0001 <= ic_in_sig when ic_we_sig = '1' else class_ic_0000;").
      get should be (
        new AssignStatement("tmp_0001",
          new WhenExpr(
            new BinaryExpr("=", new Ident("ic_we_sig"), new Constant("'1'")), // cond
            new Ident("ic_in_sig"),    // then-expr
            new Ident("class_ic_0000") // else-expr
          )
        )
      )
  }

  "expr or/and" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "test_req_local or test_req_sig").
      get should be (
        new BinaryExpr("or", new Ident("test_req_local"), new Ident("test_req_sig"))
      )
    obj.parse(obj.expression, "test_req_flag and tmp_0006").
      get should be (
        new BinaryExpr("and", new Ident("test_req_flag"), new Ident("tmp_0006"))
      )
  }

  "expr not" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "not test_req_flag_d").
      get should be (
        new UnaryExpr("not", new Ident("test_req_flag_d"))
      )
  }

  "expr +/-" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "test_ia_0004 + test_ib_0005").
      get should be (
        new BinaryExpr("+", new Ident("test_ia_0004"), new Ident("test_ib_0005"))
      )
    obj.parse(obj.expression, "test_ia_0004 - test_ib_0005").
      get should be (
        new BinaryExpr("-", new Ident("test_ia_0004"), new Ident("test_ib_0005"))
      )
  }

  "expr hex-value" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "X\"00000000\"").
      get should be (
        new BasedValue("\"00000000\"", 16)
      )
  }

  "expr concast" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "a & b").
      get should be (
        new BinaryExpr("&", new Ident("a"), new Ident("b"))
      )
  }

  "expr bit-padding" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.bit_padding_expression, "(32-1 downto 30 => '0')").
      get should be (
        new BitPaddingExpr("downto",
          new Constant("32-1"), // TODO : new BinaryExpr("-", new Constant("32"), new Constant("1")),
          new Constant("30"),
          new Constant("'0'"))
      )
  }

  "expr bit-vector-select" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.bit_vector_select, "unary_expr_00017(31 downto 2)").
      get should be (
        new BitVectorSelect(
          new Ident("unary_expr_00017"),
          "downto",
          new Constant("31"),
          new Constant("2"))
      )
  }

  "expr bit-select" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.expression, "unary_expr_00017(2)").
      get should be (
        new BitSelect(new Ident("unary_expr_00017"), new Constant("2"))
      )
  }

  "if \"if clk'event and clk = '1' then...end if;\"" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.if_statement, """
if clk'event and clk = '1' then
end if;
""").get should be (
      new IfStatement(
        new BinaryExpr("and", new Ident("clk'event"), new BinaryExpr("=", new Ident("clk"), new Constant("'1'"))),
        List(),
        List(),
        None
      )
    )
  }

  "if \"if rising_edge(clk) then...end if;\"" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.if_statement, """
if rising_edge(clk) then
end if;
""").get should be (
      new IfStatement(
        new CallExpr("rising_edge", List("clk")),
        List(),
        List(),
        None
      )
    )
  }

  "if if-then" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.if_statement, """
        if test_method = test_method_S_0000 then
          test_busy_sig <= '0';
        end if;
""").get should be (
      new IfStatement(
        new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0000")),
        List(new AssignStatement("test_busy_sig", new Constant("'0'"))),
        List(),
        None
      )
    )
  }

  "if if-then-else" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.if_statement, """
        if test_method = test_method_S_0000 then
          test_busy_sig <= '0';
        else
          test_busy_sig <= '1';
        end if;
""").get should be (
      new IfStatement(
        new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0000")),
        List(new AssignStatement("test_busy_sig", new Constant("'0'"))),
        List(),
        Some(List(new AssignStatement("test_busy_sig", new Constant("'1'")))),
      )
    )
  }

  "if if-then-elsif-else" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.if_statement, """
        if test_method = test_method_S_0000 then
          test_busy_sig <= '0';
        elsif test_method = test_method_S_0001 then
          test_busy_sig <= '1';
        else
          test_busy_sig <= '1';
        end if;
""").get should be (
      new IfStatement(
        new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0000")),
        List(new AssignStatement("test_busy_sig", new Constant("'0'"))),
        List(
          new IfStatement(
            new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0001")),
            List(new AssignStatement("test_busy_sig", new Constant("'1'"))),
            List(),
            None
          )),
        Some(List(new AssignStatement("test_busy_sig", new Constant("'1'")))),
      )
    )
  }

  "if if-then-elsif-elsif-else" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.if_statement, """
        if test_method = test_method_S_0000 then
          test_busy_sig <= '0';
        elsif test_method = test_method_S_0001 then
          test_busy_sig <= '1';
        elsif test_method = test_method_S_0002 then
          test_busy_sig <= '0';
        else
          test_busy_sig <= '1';
        end if;
""").get should be (
      new IfStatement(
        new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0000")),
        List(new AssignStatement("test_busy_sig", new Constant("'0'"))),
        List(
          new IfStatement(
            new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0001")),
            List(new AssignStatement("test_busy_sig", new Constant("'1'"))),
            List(),
            None
          ),
          new IfStatement(
            new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0002")),
            List(new AssignStatement("test_busy_sig", new Constant("'0'"))),
            List(),
            None
          )
        ),
        Some(List(new AssignStatement("test_busy_sig", new Constant("'1'")))),
      )
    )
  }

  "if if-then-elsif" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.if_statement, """
        if test_method = test_method_S_0000 then
          test_busy_sig <= '0';
        elsif test_method = test_method_S_0001 then
          test_busy_sig <= '1';
        end if;
""").get should be (
      new IfStatement(
        new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0000")),
        List(new AssignStatement("test_busy_sig", new Constant("'0'"))),
        List(
          new IfStatement(
            new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0001")),
            List(new AssignStatement("test_busy_sig", new Constant("'1'"))),
            List(),
            None
          )),
        None
      )
    )
  }

  "case statements minimal" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.case_statement, """
        case (test_method) is
        end case;
""").get should be (
      new CaseStatement(new Ident("test_method"),
        List()
      )
    )
  }

  "case statements with a when-clause" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.case_statement, """
        case (test_method) is
          when others => null;
        end case;
""").get should be (
      new CaseStatement(new Ident("test_method"),
        List(new CaseWhenClause(new Ident("others"), List(new NullStatement())))
      )
    )
  }

  "case statements" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.case_statement, """
        case (test_method) is
          when test_method_IDLE => 
            test_method <= test_method_S_0000;
          when test_method_S_0000 => 
            test_method <= test_method_S_0001;
          when test_method_S_0001 => 
            if tmp_0008 = '1' then
              test_method <= test_method_S_0002;
            end if;
          when others => null;
        end case;
""").get should be (
      new CaseStatement(new Ident("test_method"),
        List(
          new CaseWhenClause(
            new Ident("test_method_IDLE"),
            List(new AssignStatement("test_method", new Ident("test_method_S_0000")))),
          new CaseWhenClause(
            new Ident("test_method_S_0000"),
            List(new AssignStatement("test_method", new Ident("test_method_S_0001")))),
          new CaseWhenClause(
            new Ident("test_method_S_0001"),
            List(
              new IfStatement(new BinaryExpr("=", new Ident("tmp_0008"), new Constant("'1'")),
                List(new AssignStatement("test_method", new Ident("test_method_S_0002"))),
                List(),
                None))),
          new CaseWhenClause(new Ident("others"), List(new NullStatement()))
        )
      )
    )
  }

  "module instantiation (minimal)" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.instantiation_statement, """
  inst_u_synthesijer_fsub64_test : synthesijer_fsub64
  port map(
  );
""").get should be (
      new InstanceStatement(
        new Ident("inst_u_synthesijer_fsub64_test"),
        new Ident("synthesijer_fsub64"),
        List()
      )
    )
  }

  "module instantiation" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.instantiation_statement, """
  inst_u_synthesijer_fsub64_test : synthesijer_fsub64
  port map(
    clk => clk,
    reset => reset,
    a => u_synthesijer_fsub64_test_a,
    b => u_synthesijer_fsub64_test_b,
    nd => u_synthesijer_fsub64_test_nd,
    result => u_synthesijer_fsub64_test_result,
    valid => u_synthesijer_fsub64_test_valid
  );
""").get should be (
      new InstanceStatement(
        new Ident("inst_u_synthesijer_fsub64_test"),
        new Ident("synthesijer_fsub64"),
        List(
          new PortMapItem(new Ident("clk"),    new Ident("clk")),
          new PortMapItem(new Ident("reset"),  new Ident("reset")),
          new PortMapItem(new Ident("a"),      new Ident("u_synthesijer_fsub64_test_a")),
          new PortMapItem(new Ident("b"),      new Ident("u_synthesijer_fsub64_test_b")),
          new PortMapItem(new Ident("nd"),     new Ident("u_synthesijer_fsub64_test_nd")),
          new PortMapItem(new Ident("result"), new Ident("u_synthesijer_fsub64_test_result")),
          new PortMapItem(new Ident("valid"),  new Ident("u_synthesijer_fsub64_test_valid"))
        )
      )
    )
  }

  "architecture" should " be parsed" in
  {
    val obj = new VHDLParser()
    obj.parse(obj.architecture_decl, """
architecture RTL of Test000 is

  attribute mark_debug : string;

  component synthesijer_mul32
    port (
      clk : in std_logic;
      reset : in std_logic;
      a : in signed(32-1 downto 0);
      b : in signed(32-1 downto 0);
      nd : in std_logic;
      result : out signed(32-1 downto 0);
      valid : out std_logic
    );
  end component synthesijer_mul32;

  signal y_we_sig : std_logic := '0';
  signal binary_expr_00031 : signed(32-1 downto 0) := (others => '0');
  signal test_dc_0079 : std_logic_vector(64-1 downto 0) := (others => '0');

  type Type_test_method is (
    test_method_IDLE,
    test_method_S_0000,
    test_method_S_0001
  );
  signal test_method : Type_test_method := test_method_IDLE;
begin

    clk_sig <= clk;
    process begin end process;

    process(clk)
    begin
      if rising_edge(clk) then
        if test_method = test_method_S_0000 then
          test_busy_sig <= '0';
        elsif test_method = test_method_S_0001 then
          test_busy_sig <= '1';
        else
          test_busy_sig <= '1';
        end if;
      end if;
    end process;

    inst_u_synthesijer_fsub64_test : synthesijer_fsub64
    port map(
      clk => clk,
      reset => reset,
      a => u_synthesijer_fsub64_test_a,
      b => u_synthesijer_fsub64_test_b,
      nd => u_synthesijer_fsub64_test_nd,
      result => u_synthesijer_fsub64_test_result,
      valid => u_synthesijer_fsub64_test_valid
    );

end RTL;
""").get should be(
      new Architecture("RTL", "Test000",
        List(
          new Attribute("mark_debug", "string"),
          new ComponentDecl("synthesijer_mul32",
            Some(List(
              new PortItem("clk", "in", new StdLogic()),
              new PortItem("reset", "in", new StdLogic()),
              new PortItem("a", "in", new VectorKind("signed", "downto", "32-1", "0")),
              new PortItem("b", "in", new VectorKind("signed", "downto", "32-1", "0")),
              new PortItem("nd", "in", new StdLogic()),
              new PortItem("result", "out", new VectorKind("signed", "downto", "32-1", "0")),
              new PortItem("valid", "out", new StdLogic())
            ))
          ),
          new Signal("y_we_sig", new StdLogic(), Some("'0'")),
          new Signal("binary_expr_00031", new VectorKind("signed", "downto", "32-1", "0"), Some("(others=>'0')")),
          new Signal("test_dc_0079", new VectorKind("std_logic_vector", "downto", "64-1", "0"), Some("(others=>'0')")),
          new UserType("Type_test_method", List("test_method_IDLE", "test_method_S_0000", "test_method_S_0001")),
          new Signal("test_method", new UserTypeKind("Type_test_method"), Some("test_method_IDLE"))
        ),
        List(
          new AssignStatement("clk_sig", new Ident("clk")),
          new ProcessStatement(None, None, List()),
          new ProcessStatement(
            Some(List("clk")),
            None,
            List(new IfStatement(
              new CallExpr("rising_edge", List("clk")),
              List(
                new IfStatement(
                  new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0000")),
                  List(new AssignStatement("test_busy_sig", new Constant("'0'"))),
                  List(
                    new IfStatement(
                      new BinaryExpr("=", new Ident("test_method"), new Ident("test_method_S_0001")),
                      List(new AssignStatement("test_busy_sig", new Constant("'1'"))),
                      List(),
                      None
                    )),
                  Some(List(new AssignStatement("test_busy_sig", new Constant("'1'")))),
                )
              ),
              List(),
              None
            )
            )
          ),
          new InstanceStatement(
            new Ident("inst_u_synthesijer_fsub64_test"),
            new Ident("synthesijer_fsub64"),
            List(
              new PortMapItem(new Ident("clk"),    new Ident("clk")),
              new PortMapItem(new Ident("reset"),  new Ident("reset")),
              new PortMapItem(new Ident("a"),      new Ident("u_synthesijer_fsub64_test_a")),
              new PortMapItem(new Ident("b"),      new Ident("u_synthesijer_fsub64_test_b")),
              new PortMapItem(new Ident("nd"),     new Ident("u_synthesijer_fsub64_test_nd")),
              new PortMapItem(new Ident("result"), new Ident("u_synthesijer_fsub64_test_result")),
              new PortMapItem(new Ident("valid"),  new Ident("u_synthesijer_fsub64_test_valid"))
            )
          )
        )
      )
    )
  }

}
