package myFirstbot;
import robocode.Robot;
import robocode.ScannedRobotEvent;



public class SaraTestBot extends Robot {


		public void run() {
			while (true) {
				ahead(100);
				turnGunRight(360);
				back(50);
				turnGunRight(360);
				}
			
			
		}
		public void onScannedRobot(ScannedRobotEvent e) {
			fire(3);
		}

}
