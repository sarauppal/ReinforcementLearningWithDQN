package myFirstbot;

public class utils {
	
	public utils () {
		
	}
	
	public double getRandomNumber(int min, int max)
	{

		return (int)(Math.random() * ((max-min)) + min);
				
	}
	
	public int [] probVectorGen(double prob)
	{	
		int [] arr = new int [10];
		
		for (int i =0 ; i < 10 ; i++)
		{
			if (i < 10*prob)
			{
				arr[i] = 0;
			}
			else
			{
				arr[i] = 1;
			}
		}
		
		return arr;
	}
	
	public int GenRandomStatewithProb(double prob)
	{	
		int [] arr = probVectorGen(prob);
		int rd = (int)getRandomNumber(0,10);
		return arr[rd];
	}
}
