public class Test001{
	
	private int x = 0;
	public int acc(int y){
		x += y; 
		return x;
	}
	
	public int add(int x, int y){
		return x + y;
	}
	
	public int add2(int x, int y){
		return add(x, y);
	}

	public int acc2(int num, int y){
		for(int i = 0; i < num; i++){
			x += y;
		}
		return x;
	}
	
	public int acc3(int num, int y){
		int i = 0;
		while(i < num){
			x += y;
			i++;
		}
		return x;
	}

	public int acc4(int num, int y){
		int i = 0;
		do{
			x += y;
			i++;
		}while(i < num);
		return x;
	}

	public int switch_test(int x){
	    int value = 0;
	    switch(x){
		case 0:
			return 10;
		case 1:
			return 3 + 4;
		case 2:
		{
			int i = 300;
			int j = 400;
			return i + j;
		}
		case 3:
			value = 100;
			break;
		case 4:
		{
			value = 50;
			value = 70;
		}
		case 5:
			value = 10;
		case 6:
			value = 10;
		default:
			value = 200;
		}
		return value;
	}

	
}
