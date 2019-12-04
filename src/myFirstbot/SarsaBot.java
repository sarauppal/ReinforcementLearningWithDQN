package myFirstbot;
import java.awt.Color;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections; 

import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.BattleResults;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.control.events.BattleCompletedEvent;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.HitByBulletEvent;
import robocode.BulletHitEvent;
import robocode.DeathEvent;


//import myFirstbot.LUT;

public class SarsaBot extends AdvancedRobot{
	
	static public enum enumEnergy {low, medium, high};
	static enum enumDistance {close, near, far};
	static enum enumActions {strafe, retreat, advance, fire};
	
	
	enumEnergy currEnergy = enumEnergy.high;
	enumDistance currDistance = enumDistance.close;
	enumActions currAction = enumActions.strafe;
	enumActions nextAction = enumActions.fire;
	
	enumEnergy prevEnergy;
	enumDistance prevDistance;
	enumActions prevAction;

	
	utils utility = new utils();
	static double alpha = 0.5;
	static double gamma = 0.6;
	static public double epsilon = 0.4;
	
	static double numWins = 0;
	static double numLosses = 0;
	static double [] winrate = new double [1000];
	static double [] lossrate = new double [1000];
	static boolean learning = false;
//	
//	static enum policy {on,off};
//	policy currPolicy = policy.off;
	
	
	
	public enum modes {scan, performAction};
	modes currMode = modes.scan;
	
	public double badInterimReward = -10;
	public double goodInterimReward = 10 ;
	public double badTerminalReward = -50 ;
	public double goodTerminalReward = 100 ;
	
	


	public double interimReward = 0;
	public int greedy_or_random;
	
	
	public int [] previous_state_action;
	
	static ScannedRobotEvent scannedRobot ;

	
	private double EnemyDistance;

	
	
	static LUT LUtable = new LUT (enumEnergy.values().length, enumDistance.values().length, enumActions.values().length,learning );
	

	
	public void run() {
		// first thing first, lets make the ROBOT pretty !
		setColors(new Color(255, 255, 204), new Color(51, 20, 255), new Color(51, 153, 255), null, new Color(0, 0, 153));
		if (learning == false) 
		{
			try {
				loadLUT();
				}
			
			catch (IOException except) 
				{
				except.printStackTrace();
				}
		}

		while (true) 
		{
			System.out.println("back in normal mode");
			
			
			
			switch (currMode)
			{
			case scan:
				{
				System.out.println( " switch scanning");
				turnGunRight(180);
				break;
				}
				
			case performAction:
				{
				// place holder for case perform action
				System.out.println("entered perform action");
				System.out.println(" going to " + currAction);
				switch(currAction)
				{
				case strafe:
					{System.out.println("calling strafe from switch");
					System.out.println(scannedRobot.getBearing());
					strafe(scannedRobot.getBearing());
					break;}
				case advance:
					{System.out.println("calling advance from switch");
					System.out.println(scannedRobot.getBearing());
					advance(scannedRobot.getBearing());
					break;}
				case retreat:
					{System.out.println("calling retreat from switch");
					retreat(scannedRobot.getBearing());
					break;}
				case fire:
					{System.out.println("calling fire from switch");
					double turnGunAmt = (getHeadingRadians() + scannedRobot.getBearingRadians()) - getGunHeadingRadians();
					
					int firePower;
					if (scannedRobot.getDistance() > 500){firePower = 1;}
					else if (scannedRobot.getDistance() > 100 ){firePower = 2;}
					else {firePower = 3;}
					fires(firePower,turnGunAmt);
					break;}		
					}			
				
				System.out.println("Updating values" + scannedRobot.getEnergy());
				QuantizeEnergy(scannedRobot.getEnergy()); // this method also changes the value of currEnergy
				QuantizeDistance(scannedRobot.getDistance()); //this method also changes the value of currDistance internally
				
				System.out.println("action done, new state " + currEnergy + " " + currDistance);
				
				if (learning == true)
				{
					nextAction = enumActions.values()[getNextAction(currEnergy.ordinal(),currDistance.ordinal())];
				}
				else 
				{
					// get from LUT
					nextAction = enumActions.values()[ActionFromLUT(currEnergy.ordinal(),currDistance.ordinal())];
				}
				
				
				
				System.out.println("Update Q table ");
				computeQ_SARSA();
				interimReward=0;
				currMode = modes.scan;
				
			
			}
				
			}
		}
			
			
		
		
	}
		
	public void onScannedRobot(ScannedRobotEvent e) {
		scannedRobot = e;
		// move current state to previous state
		prevEnergy = currEnergy;
		prevDistance = currDistance;
		prevAction = currAction;
		System.out.println("Found Bot");
//		fire(3);
		// put robot in perform action

		// choose an action
		System.out.println( "Choosing action");
		
		currAction = nextAction;
		// perform action
		currMode = modes.performAction;
		System.out.println("set mode to perform action");
		
	}
	public int ActionFromLUT(int EnergyOrdinal, int DistanceOrdinal)
	{
		
		System.out.println("Getting action from LUT ");
		int idx_largest = getIndexofLargest(LUtable.LUTable[EnergyOrdinal][DistanceOrdinal]);
		System.out.println("Got" + enumActions.values()[idx_largest]);
		return idx_largest;
		
	}
	public int getNextAction(int EnergyOrdinal, int DistanceOrdinal)
	{
		greedy_or_random = utility.GenRandomStatewithProb(epsilon);
		if (greedy_or_random == 0)
		{
			System.out.println("Going random");
			 return (int)utility.getRandomNumber(0, 4);
	
		}
		
		else 
		{
			// implement greedy action, taking max
			
			System.out.println("Going greedy");
			int idx_largest = getIndexofLargest(LUtable.LUTable[EnergyOrdinal][DistanceOrdinal]);
			return idx_largest;
		
		}
		
	}
	public void onWin(WinEvent event)
	{
		
		interimReward = goodTerminalReward;
	}
	
	public void onKeyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case VK_UP:
			System.out.println("UP pressed, increase epsilon");
			setColors(new Color(102, 255, 102), new Color(51, 20, 255), new Color(51, 153, 255), null, new Color(0, 0, 153));
			System.out.println("Epsilon was " + epsilon );
			epsilon = 0.9;
			System.out.println("Epsilon now " + epsilon );
			break;

		case VK_DOWN:
			System.out.println("DOWN pressed, decrease epsilon");
			System.out.println("Epsilon was " + epsilon );
			epsilon = 0.0;
			System.out.println("Epsilon now " + epsilon );
			setColors(new Color(255, 51, 51), new Color(51, 20, 255), new Color(51, 153, 255), null, new Color(0, 0, 153));
		
			break;

		}
	}
	public int getIndexofLargest(Double [] arr) 
	{	
		
		if ( arr == null || arr.length == 0 ) return -1;
		
		int idx_largest = 0;
		for (int i = 0; i <arr.length; i++)
		{
			if ( arr[i] > arr[idx_largest]) {idx_largest = i;}
			
		}
		return idx_largest;
	}
	
	public void computeQ()
	{
		double Qvalue_prev;
		Qvalue_prev = LUtable.LUTable[prevEnergy.ordinal()][prevDistance.ordinal()][currAction.ordinal()];
		System.out.println("Q table entry before" + Qvalue_prev );
		Double [] state_curr = LUtable.LUTable[currEnergy.ordinal()][currDistance.ordinal()];
		double max = Collections.max(Arrays.asList(state_curr));
		// set new Q value
		LUtable.LUTable[prevEnergy.ordinal()][prevDistance.ordinal()][currAction.ordinal()] = Qvalue_prev + (alpha * (interimReward+(gamma * max) - Qvalue_prev));
		System.out.println("Q table entry now " + LUtable.LUTable[prevEnergy.ordinal()][prevDistance.ordinal()][currAction.ordinal()] );
	}
	
	public void computeQ_SARSA()
	{
		double Qvalue_prev;
		Qvalue_prev = LUtable.LUTable[prevEnergy.ordinal()][prevDistance.ordinal()][currAction.ordinal()];
		System.out.println("Q table entry before" + Qvalue_prev );
		Double Q_onpolicy = LUtable.LUTable[currEnergy.ordinal()][currDistance.ordinal()][nextAction.ordinal()];
		// set new Q value
		LUtable.LUTable[prevEnergy.ordinal()][prevDistance.ordinal()][currAction.ordinal()] = Qvalue_prev + (alpha * (interimReward+(gamma * Q_onpolicy) - Qvalue_prev));
		System.out.println("Q table entry now " + LUtable.LUTable[prevEnergy.ordinal()][prevDistance.ordinal()][currAction.ordinal()] );
	}
	
	public void fires(int power, double turnGunAmt)
	{	
		System.out.println("inside fire");
	
	setTurnGunRightRadians(turnGunAmt);
	
	fire(power);
	execute();
	
	

	}
	

	public void strafe(double EnemyBearing)
	{	System.out.println("inside strafe");
		double moveDirection = 1;
		setTurnRight(normalizeBearing(EnemyBearing) + 90);
		setAhead(150);
		execute();
//		if (getTime() % 1 == 0) {
//			moveDirection *= -1;
//			setAhead(150 * moveDirection);
//			}
//		execute();
		
	}
	
	public void advance(double EnemyBearing)
	{
		System.out.println("inside advance");
		System.out.println("Bearing seen " + EnemyBearing);
		setTurnRight(normalizeBearing(EnemyBearing));
		setAhead(EnemyDistance/2 + 1);
		execute();
	}
	
//	 normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		System.out.println("Normalized angle : " + angle);
		return angle;
	}
	
	public void retreat (double EnemyBearing)
	{
		System.out.println("inside retreat");
		System.out.println("Bearing seen : " + EnemyBearing);		
		turnRight(normalizeBearing(EnemyBearing+180));
		ahead(100);
	}
		
	public void onHitWall(HitWallEvent e) { 
		turnRight (e.getBearing()+180);
		ahead(10);
		}
	
//	Intermediate rewards	
	 public void onBulletHit(BulletHitEvent event) 
	 {
	       out.println("I hit " + event.getName() + "!");
	       
		interimReward = goodInterimReward ;
		System.out.println("Reward received " + interimReward);
	}
	
	 public void onHitByBullet(HitByBulletEvent event) 
	 {
		 
	       out.println(event.getName() + " hit me!");
	       interimReward = badInterimReward;
		System.out.println("Reward received " + interimReward);
	   }
	 
	 public void onRobotDeath(RobotDeathEvent event) 
	 {
		interimReward = goodTerminalReward ;
		System.out.println("Reward received " + interimReward);
		numWins += 1;
		 
	 }
	 
	 public void onDeath(DeathEvent event) 
	 {
		 numLosses += 1;
		 interimReward = badTerminalReward ;
		
	 }
	 
	 public void saveInterimResults()
	 {   System.out.println("from inside saveInterim " + getRoundNum());
		 if ((getRoundNum()+1) % 10 == 0)
		 {
			 int idx = ((getRoundNum()+1)/10 -1 )  ;
			 System.out.println("idx calculated" + idx);
			 winrate[idx] = numWins/(getRoundNum()+1);
			 lossrate[idx] = numLosses/(getRoundNum()+1);
		
		 }
		 
	 }
	  public void onRoundEnded(RoundEndedEvent event) {
		  
	       out.println("The round has ended" + getRoundNum());
	       saveInterimResults();
//	       if ((getRoundNum()+1) % 100 == 0)
//	       {
//	    	   saveLUT();
//	       }
	   }
	 public void loadLUT() throws IOException
	 {
		 BufferedReader reader = new BufferedReader(new FileReader(getDataFile("LUT.txt")));
			String line = reader.readLine();
		
			try {
		   
		        
			        
		        	for (int i = 0; i <3; i++)
		        	{
		        		for (int j = 0; j<3; j++)
		        		{
		        			for (int k = 0; k < 4; k++)
		        			{	
		        				if (line != null) 
		        					{
		        					LUtable.LUTable[i][j][k] = Double.parseDouble(line);
		        					System.out.println(i+" " + j + " " + k + " " +line);
		        					line= reader.readLine();
		        					}
		        			}
		        		}
		        	}     
			        
				}
		        
			catch (IOException e) 
				{
				e.printStackTrace();
				}
			finally 
				{
				reader.close();
				}
	 }
	 public void saveLUT()
	 {
		 try
			{
	    	   System.out.println("trying to save LUT");
	    	   PrintWriter pr = new PrintWriter((new RobocodeFileOutputStream(getDataFile("LUT.txt"))));   

//			   
			    for (int i = 0; i< LUtable.LUTable.length; i++)
			    {
//			    	pr.println("For Energy " + i);
			    	for (int j = 0; j<LUtable.LUTable[i].length; j++)
			    	{
//			    		pr.println("For Distance" + j);
			    		for (int k = 0; k < LUtable.LUTable[i][j].length; k++)
			    		{
			    			pr.println( LUtable.LUTable[i][j][k]);
			    		}
//			    		pr.println(Arrays.toString(LUtable.LUTable[i][j]));
//			    		pr.println();
			    	}
			    	
//			    	pr.println();
			   
			    }
			    pr.close();
			   
			}
			catch (Exception except)
			
			{
				System.out.println("coudn't");
				except.printStackTrace();
			    System.out.println("No such file exists.");
			}
	       
	 }
	 public String QuantizeEnergy(double value ) 
	 {
		 if (value > 80)
		 {
			 currEnergy = enumEnergy.high;
			 return "high";
		 }
		 else if (value > 40 )
		 {
			 currEnergy = enumEnergy.medium;
			 return "medium";
			 
		 }
		 
		 else if (value <= 40 )
		 {
			 currEnergy = enumEnergy.low;
			 return "low";
		 }
		 
		 else 
		 {
			 
			 throw new IllegalArgumentException("Non number passed");
			 
		 }
		 
	 }
	 
	 public String QuantizeDistance(double value ) 
	 {
		 if ( value > 500 )
		 {
			 currDistance = enumDistance.far;
			 return "far";
		 }
		 else if ( value > 200 )
		 {
			 currDistance = enumDistance.near;
			 return "near";
		 }
		 
		 else if ( value <= 200 )
		 {
			 currDistance = enumDistance.close;
			 return "close";
			 
		 }
		 
		 else 
		 {
			 throw new IllegalArgumentException("Non number passed");
		 }
	 }
	 
	 public void onBattleEnded(BattleEndedEvent e) {
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.CEILING);
		saveLUT();
		System.out.println("****** see meee******"+getNumRounds());
		try
		{
		    PrintWriter pr = new PrintWriter((new RobocodeFileOutputStream(getDataFile("numRounds_"+getNumRounds()+"_WinLossRate_alpha_"+alpha+"_gamma_"+gamma+"_learning_"+learning+"_epsilon_"+epsilon+"GI,BI,GT,BT"+goodInterimReward+"-"+badInterimReward+"-"+goodTerminalReward+"-"+badTerminalReward+"crazy"+".txt"))));    

		    pr.println("NumRounds	Winrate		LossRate");
		    for (int i=0; i<winrate.length; i++)
		    {
		    	pr.println(i*10 + "		" + df.format(winrate[i]) + "		" + df.format(lossrate[i]));
		    }
		    
		    pr.println("numWins" + numWins);
		    pr.println("numLosses" + numLosses);
		    
		    
//		    pr.println(Arrays.toString(lossrate));
////		    }
		    pr.close();
		   
		}
		catch (Exception except)
		
		{
			System.out.println("coudn't");
			except.printStackTrace();
		    System.out.println("No such file exists.");
		}
		
		 

		}
		 
	 
	 


	
}
