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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

import networks.MLP;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.RoundEndedEvent;
import robocode.AdvancedRobot;
import robocode.BattleEndedEvent;
import robocode.BattleResults;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.control.events.BattleCompletedEvent;
import utilities.Matrix;
import robocode.HitWallEvent;
import robocode.RobocodeFileOutputStream;
import robocode.HitByBulletEvent;
import robocode.BulletHitEvent;
import robocode.DeathEvent;


//import myFirstbot.LUT;

public class SarasAnnhilatorActionNN extends AdvancedRobot{
	
//	static public enum enumEnergy {low, medium, high};
//	static enum enumDistance {close, near, far};
	static enum states {energy , distance};
	static enum enumActions {strafe, retreat, advance, fire};
	
	
//	enumEnergy currEnergy = enumEnergy.high;
//	enumDistance currDistance = enumDistance.close;
	enumActions currAction = enumActions.strafe;
	
	public float currEnergy = 100.0f;
	public float currDistance = 500.0f;
	
	
//	enumEnergy prevEnergy;
	public float prevEnergy;
//	enumDistance prevDistance;
	public float prevDistance;
	enumActions prevAction;
	enumActions nextAction;
	
	utils utility = new utils();
	static double alpha = 0.5;
	static float gamma = 0.8f;
	static public double epsilon = 0.8;
	
	static double numWins = 0;
	static double numLosses = 0;
	static double [] winrate = new double [400];
	static double [] lossrate = new double [400];
	static boolean learning = true;
	
	
	
	public enum modes {scan, performAction};
	modes currMode = modes.scan;
	
	public float badInterimReward = -2;
	public float goodInterimReward = 5 ;
	public float badTerminalReward = -20 ;
	public float goodTerminalReward = 30 ;
	
	


	public float interimReward = 0;
	public int greedy_or_random;
	
	
	public int [] previous_state_action;
	
	static ScannedRobotEvent scannedRobot ;

	static int experienceQsize = 1;
	private double EnemyDistance;

	// initializing NN here
	
	
	static Matrix prev_state_vector = new Matrix(experienceQsize,states.values().length);
	static Matrix curr_state_vector = new Matrix(experienceQsize,states.values().length);
	static Matrix Q_target = new Matrix(experienceQsize,enumActions.values().length);
	
	static int inputs = prev_state_vector.columns;
    static int hidden1 = 10;
    static int hidden2 = 10;
    static int outputs = Q_target.columns;
//    int epochs = 20000;
    static float lr = 0.05f;
    
    static int[] networkShape = {inputs, hidden1,hidden2, outputs};
    static MLP nn = new MLP(networkShape, lr);
	
	
	public void run() {
		// first thing first, lets make the ROBOT pretty !
		setColors(new Color(255, 255, 204), new Color(51, 20, 255), new Color(51, 153, 255), null, new Color(0, 0, 153));
//		ArrayList<Matrix> weights_before = nn.w;
//		System.out.println("Weights before");
//		for (Matrix w : weights_before) {
//			System.out.println(w);
//		}
//		
//		try {
//			loadWeights();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		while (true) 
		{
		
			System.out.println("back in normal mode");
			
			
			
			switch (currMode)
			{
			case scan:
				{
				System.out.println( "switch scanning");
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
				
				
				currEnergy = (float) scannedRobot.getEnergy();
				currDistance = (float) scannedRobot.getDistance();
				
			
				curr_state_vector.values[0][0] = currEnergy;
				curr_state_vector.values[0][1] = currDistance;
				
				System.out.println("action done, new state " + currEnergy + " " + currDistance);
				
				System.out.println("Compute Bellman Q and BP ");
				ComputeBellmanQandBP();
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
		prev_state_vector.values[0][0] = prevEnergy;
		prev_state_vector.values[0][1] = prevDistance;
		
		
		
		prevAction = currAction;
		System.out.println("Found Bot");
		fire(3);

		// choose an action
		System.out.println( "Choosing action");
		
		if (learning == true)
			{
			greedy_or_random = utility.GenRandomStatewithProb(epsilon);
			}
		else
			{
			// If greedy always take the maximum
			greedy_or_random = 1;
			try {
				loadWeights();
				}
			
			catch (IOException except) 
				{
				except.printStackTrace();
				}
			}
		
		if (greedy_or_random == 0)
		{
			System.out.println("Going random");
			currAction = enumActions.values()[(int)utility.getRandomNumber(0, 4)];
			System.out.println("Peforming " + currAction);
		}
		
		else 
		{
			// implement greedy action, taking max
			
			System.out.println("Going greedy");
			// forward propagate state it is in and find the greedy action
			int idx_largest = nn.forwardPropagate(prev_state_vector).IdxmaxAxis()[0];
			System.out.println("max rows indexes");
			System.out.println(Arrays.toString(nn.forwardPropagate(prev_state_vector).IdxmaxAxis()));
			currAction = enumActions.values()[idx_largest];
			System.out.println("Peforming " + currAction);
			
		}
		
	
		
		currMode = modes.performAction;
		System.out.println("set mode to perform action");
		
	
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
			if ( arr[i] > arr[idx_largest]) idx_largest = i;
			
		}
		return idx_largest;
	}
	
	public double ComputeBellmanQandBP()
	{
		float target;

		
		target = interimReward + gamma*(nn.forwardPropagate(curr_state_vector).maxAxis()[0]);
		Q_target.values[0][currAction.ordinal()] = target;
		System.out.println(prev_state_vector);
		System.out.println(Q_target);
		nn.backPropagate(prev_state_vector, Q_target);
		
		
		return target;
	}
	

	
	
	public void fires(int power, double turnGunAmt)
	{	
	
	
	setTurnGunRightRadians(turnGunAmt);
	
	fire(power);
	execute();
	
	

	}
	

	public void strafe(double EnemyBearing)
	{	

		setTurnRight(normalizeBearing(EnemyBearing) + 90);
		setAhead(150);
		execute();

		
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
	 public void loadWeights() throws IOException
	 {	System.out.println("inside load weights");
		 BufferedReader sc = new BufferedReader(new FileReader(getDataFile("Weights.txt")));
		 String line = sc.readLine();
		 ArrayList<Matrix> weights = nn.w;
			try {
		 
				for (Matrix weight : weights)
			    {
					
					for (int i = 0; i<weight.rows ; i++) {

						for (int j=0 ; j<weight.columns;j++) {
							if (line != null) {
								weight.values[i][j] = (float) Double.parseDouble(line);
								line= sc.readLine();
							}
							
						}
			
					}
					
			    }
				
				// check weights
				ArrayList<Matrix> weights_check = nn.w;
				for(Matrix chk : weights_check) {
					System.out.println(chk);
				}
				
			}
			catch (IOException e) 
			{
			e.printStackTrace();
			}
		finally 
			{
			sc.close();
			}
	 }
	 public void saveWeights()
	 { 	ArrayList<Matrix> weights = nn.w;
		 try
			{
	    	   System.out.println("trying to save Weights");
	    	   PrintWriter pr = new PrintWriter((new RobocodeFileOutputStream(getDataFile("Weights.txt"))));   
			   
			    for (Matrix weight : weights)
			    {
			    	
			    	for(int i = 0; i < weight.rows ; i ++) {
			    		for(int j = 0 ; j < weight.columns ; j++) {
			    			pr.println(weight.values[i][j]);
			    		}
			    	}
	
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
	 
	 
	 public void onBattleEnded(BattleEndedEvent e) {
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.CEILING);
		saveWeights();

		
		try
		{
		    PrintWriter pr = new PrintWriter((new RobocodeFileOutputStream(getDataFile("NNnumRounds_"+getNumRounds()+"_WinLossRate_alpha_"+alpha+"_gamma_"+gamma+"_learning_"+learning+"_epsilon_"+epsilon+"GI,BI,GT,BT"+goodInterimReward+"-"+badInterimReward+"-"+goodTerminalReward+"-"+badTerminalReward+"crazy"+".txt"))));    

		    pr.println("NumRounds	Winrate		LossRate");
		    for (int i=0; i<winrate.length; i++)
		    {
		    	pr.println(i*10 + "		" + df.format(winrate[i]) + "		" + df.format(lossrate[i]));
		    }
		    
		    pr.println("numWins" + numWins);
		    pr.println("numLosses" + numLosses);
		    
		    

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
