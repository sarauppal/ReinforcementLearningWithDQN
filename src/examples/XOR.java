package examples;

import networks.MLP;
import utilities.Matrix;
import java.io.*; 
import java.util.Arrays;


public class XOR {

    public static void main(String[] args) throws IOException {
//        Matrix x = new Matrix(new float[][] {{84.000f,  730.364f },
//        									{97.000f,  679.954f },
//        									{100.000f,  513.530f },
//        									{97.000f,  679.954f }});
//        
//        Matrix y = new Matrix(new float[][] {{-0.653f,  0.128f, -0.209f,  8.969f },
//        		 {-1.050f,  8.969f, -0.045f,  1.583f },
//        				 {-1.328f, -0.117f,  2.015f,  3.969f },
//        				 { 0.423f,  8.969f, -1.011f,  0.856f }});
	     
        int [] Domains = {3 , 3};
        Matrix x = new Matrix(Domains, true);
        x = x.Normalize();


		Matrix y = new Matrix(9, 4, "LUT.txt");


		

        int inputs = x.columns;
        int hidden1 = 20;
        int hidden2 = 20;
        int outputs = y.columns;
        int epochs = 100000;
        float lr = 0.05f;
        
        int[] networkShape = {inputs, hidden1,hidden2, outputs};
//        int[] networkShape = {inputs, hidden1, outputs};
        
        MLP nn = new MLP(networkShape, lr);
        
        nn.train(x, y, epochs);
        System.out.println(nn.predict(x));
    }

}
