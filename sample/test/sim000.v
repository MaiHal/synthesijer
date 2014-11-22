`default_nettype none
`timescale 1ns/1ns
module sim000();

  reg  clk = 1'b0;
  reg  reset = 1'b0;
  reg signed [32-1 : 0] counter = 0;
  parameter main_IDLE = 32'd0;
  parameter main_S0 = 32'd1;
  reg [31:0] main = main_IDLE;
  reg signed [32-1 : 0] main_delay = 0;
  wire signed [32-1 : 0] tmp_0001;
  wire  tmp_0002;
  wire  tmp_0003;
  wire  tmp_0004;
  wire  tmp_0005;

  assign tmp_0001 = counter + 1;
  assign tmp_0002 = counter > 3 ? 1'b1 : 1'b0;
  assign tmp_0003 = counter < 8 ? 1'b1 : 1'b0;
  assign tmp_0004 = tmp_0002 && tmp_0003;
  assign tmp_0005 = tmp_0004 == 1'b1 ? 1'b1 : 1'b0;

  always begin
  // state main = main_IDLE
  #10
  main <= main_S0;
  // state main = main_S0
  #10
  main <= main_IDLE;
  end

  always @(main) begin
     if (main == main_IDLE) begin
      clk <= 1'b0;
    end else if (main == main_S0) begin
      clk <= 1'b1;
    end
  end

  always @(main) begin
    reset <= tmp_0005;
  end

  always @(main) begin
     if (main == main_IDLE) begin
      counter <= tmp_0001;
    end else if (main == main_S0) begin
      counter <= tmp_0001;
    end
  end

   wire req_000;
      
   assign req_000 = counter > 50 ? 1'b1 : 1'b0;
   
   Test000 U000(.clk(clk),
		.reset(reset),
		.x_in(32'd0),
		.x_we(1'b0),
		.x_out(),
		.y_in(64'd0),
		.y_we(1'b0),
		.y_out(),
		.ic_in(32'd0),
		.ic_we(1'b0),
		.ic_out(),
		.lc_in(64'd0),
		.lc_we(1'b0),
		.lc_out(),
		.test_ib(32'd3),
		.test_ia(32'd100),
		.test_lb(64'd3),
		.test_la(64'd100),
		.test_req(req_000),
		.test_busy()
		);
   
endmodule
`default_nettype wire
