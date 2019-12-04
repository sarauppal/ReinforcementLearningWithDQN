package myFirstbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import robocode.RobocodeFileOutputStream;
import robocode.AdvancedRobot;


public class LUT implements LUTInterface
{	
	private int domV1;
	private int domV2;
	private int domV3;
	private boolean learn; 
	public Double [][][] LUTable;
	

	public LUT (int domV1, int domV2, int domV3 , boolean learn)
	{
		this.domV1 = domV1;
		this.domV2 = domV2;
		this.domV3 = domV3;
		LUTable = new Double [domV1][domV2][domV3];
		this.learn =learn;
		if (learn == true)
		{
			this.initialiseLUT();		
		}
		
	}
			
	
	public void initialiseLUT()
	{
		//figure out a way to initialize variable D array
		//idea merge all states together and every time declare a 2D array
		for (int i = 0; i < domV1; i++)
		{
			for (int j = 0 ; j < domV2; j++)
			{
				for (int k = 0 ; k < domV3; k++)
				{
					LUTable[i][j][k] = 0.0 ;
				}
			}
		}
		System.out.println("LUT initialised");
		
				
	}
	
	public int indexFor(double [] X)
	{
		
		return 0;
		
	}
	
	public double outputFor(double [] X)
	{
		return 0.0;
	}
	
	public double train(double [] X, double argValue)
	{
		return 0.0;
	}

	/**
	* A method to write either a LUT or weights of an neural net to a file.
	* @param argFile of type File.
	*/
	public void save(File argFile)
	{
//		 RobocodeFileOutputStream f	 = null;
//		try
//		{
//			f = new RobocodeFileOutputStream(argFile);
//		    PrintWriter pr = new PrintWriter(f);    
//
////		   
//		    for (int i = 0; i< this.LUTable.length; i++)
//		    {
//		    	pr.println("For Energy " + i);
//		    	for (int j = 0; j<this.LUTable[i].length; j++)
//		    	{
//		    		pr.println("For Distance" + j);
//		    		pr.println(Arrays.toString(this.LUTable[i][j]));
//		    	}
//		    	
//		   
//		    }
//		    pr.close();
//		   
//		}
//		catch (Exception except)
//		
//		{
//			System.out.println("coudn't");
//			except.printStackTrace();
//		    System.out.println("No such file exists.");
//		}
//		
	}
	/**
	* Loads the LUT or neural net weights from file. The load must of course
	* have knowledge of how the data was written out by the save method.
	* You should raise an error in the case that an attempt is being
	* made to load data into an LUT or neural net whose structure does not match
	* the data in the file. (e.g. wrong number of hidden neurons).
	* @throws IOException
	*/
	public void load(String argFileName) throws IOException
	{
		
		
	}
} 
