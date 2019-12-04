package myFirstbot;
import java.util.Arrays;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class SarasAnnhilatorTest {


	
	@Test
	public void RandomGenTest()
	{	double rd;
		utils myfuncs = new utils();
		rd = myfuncs.getRandomNumber(5, 10);
//		System.out.println(rd);
		Assert.assertEquals(1, 1, 0.001);
		
	}
	
	@Test
	public void ProbVectorTest()
	
	{	
		int [] vector;
		utils funcs = new utils();
		vector = funcs.probVectorGen(0.1);
//		System.out.println(Arrays.toString(vector));
		
		
	}
	
	@Test 
	public void TestRandonStateGen()
	{	
		utils funcs = new utils();
		int count0 = 0;
		int count1 = 1;
		double ratio;
		for (int i = 0 ; i <500; i ++)
		{
			int state;
			state = funcs.GenRandomStatewithProb(0.8);
			if (state == 0)
			{
				count0+=1;
			}
			else
			{
				count1+=1;
			}
		}
			ratio = (double)count0/500;
			System.out.println("Zeros " + count0);
			System.out.println("Ones " + count1);
			System.out.println("Ratio " + ratio);
	}

}
