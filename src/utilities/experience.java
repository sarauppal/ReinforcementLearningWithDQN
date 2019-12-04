package utilities;

import java.util.Arrays;

public class experience {
	

	
	public final float[] state_prev;
	 
	public final float[] state_curr;
//	public final myFirstbot.SarasAnnhilatorActionNN_ER.enumActions currAction;
	public final utilities.enumActions currAction;
	public final float reward;
	
	public experience(float[] state_prev, utilities.enumActions currAction, float reward, float[] state_curr ) {
		this.state_prev = state_prev;
		this.currAction = currAction;
		this.reward = reward;
		this.state_curr = state_curr;
		
	}
	
	@Override
    public String toString() {
        String output = "[";
        	output += Arrays.toString(this.state_prev);
        	output += ",";
        	output += this.currAction;
        	output += ",";
        	output += this.reward;
        	output += ",";
        	output += Arrays.toString(this.state_curr);
            output += "]\n ";
        
        return output;
    }
}
