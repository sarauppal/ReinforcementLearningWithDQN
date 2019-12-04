package utilities;

import utilities.Initializers.Init;
import java.util.Arrays;
import java.io.*; 

public class Matrix {
    
    public final float[][] values;
    public final int rows;
    public final int columns;
    
    public Matrix(float[][] matrix) {
        this.values = matrix;
        this.rows = matrix.length;
        this.columns = matrix[0].length;
    }
    
    public Matrix(int rows, int columns, Init initializer) {
        this.values = Initializers.init(rows, columns, initializer);
        this.rows = rows;
        this.columns = columns;
    }
    public Matrix(int rows, int columns) {
        this.values = Initializers.init(rows, columns, Init.NORMAL);
        this.rows = rows;
        this.columns = columns;
    }
    
    public Matrix( int [] varDom, boolean state) {
        this.rows = StateSpace(varDom);
        this.columns = varDom.length + 1 ; // add 1 for bias
        this.values = Initializers.init(this.rows, this.columns, Init.NORMAL);
    	int s = 0; // cotrolling which row we are at
//    	int [] arr = new int [4];
        	for ( int i = 0 ; i < varDom[0] ; i++) 
        	{
        		for (int j = 0 ; j < varDom[1] ; j++)
        		{
//        			for (int k = 0 ; k < varDom[2] ; k++) {
        					
        					this.values[s][0] =  deQuantizeEnergy(i);	
        					this.values[s][1] =  deQuantizeDistance(j);
        					this.values[s][2] = 1; // bias
        			
//        					arr = oneHotEncode(k);
//        					for (int l = 0; l < arr.length ; l++) {
//        						this.values[s][2+l] = arr[l];
//        					}
        							
        					s += 1 ;
        					
        				
        			
        		}
        	}
//    	System.out.println(Arrays.deepToString(this.values));
    }
    public Matrix(int rows, int columns, String filename) throws FileNotFoundException
    {
    	this.columns = columns;
    	this.rows = rows;
    	this.values = Initializers.init(rows, columns, Init.NORMAL);
    	File file = new File("C:\\cpen502\\NNBot\\" + filename);
    	BufferedReader br = new BufferedReader(new FileReader(file));
    	String line;
		try {
			line = br.readLine();
	    	for (int i = 0; i < rows; i++) {
	    		for (int j = 0 ; j < columns ; j ++ )
	    		{
	    			if (line != null) {
		 				this.values[i][j] = Float.parseFloat(line);
		 				line= br.readLine();
		    		}
	    			
	    		}
	    		
	    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(Arrays.deepToString(this.values));	
    }
    public int StateSpace(int [] arr) {
    	int states = 1;
    	for (int i = 0; i < arr.length; i++) {
    		states = states * arr[i];
    	}
    	return states;
    }
    
    public int deQuantizeEnergy(int energy) {
    	if (energy == 0) {
    		return 40;
    		}
    	else if(energy == 1) {
    		return 80;
    		}	
    	else {
        	return 100;
    			
    	}
    	
    		
    }
   public int [] oneHotEncode(int action)
   {
	   int [] encoded = new int [4];
	   for (int i = 0 ; i < 4 ; i++) {
		   if (i== action) {
			   encoded[i] = 1;
		   }
		   else {
			   encoded[i] = 0;
		   }
	   }
	   return encoded;
   }
   public int deQuantizeDistance(int distance) {
	   if (distance == 0) {
   		return 200;
   		}
   	else if(distance == 1) {
   		return 500;
   		}	
   	else {
       	return 1000;
   		}	
    }
    


    
    public Matrix transpose() {
        float[][] result = new float[this.values[0].length][this.values.length];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[j][i] = values[i][j];
            }
        }
        return new Matrix(result);
    }
    
    public Matrix T() {
        return transpose();
    }
    
    public Matrix sum() {
        float[][] result = new float[this.rows][1];
        for (int i = 0; i < rows; i++) {
            result[i][0] = 0;
            for (int j = 0; j < columns; j++){
                result[i][0] += this.values[i][j];
            }
        }
        return new Matrix(result);
    }
    
    public Array sumaxis() {
        float[] result = new float[this.columns];
        for (int i = 0; i < columns; i++) {
            result[i] = 0;
            for (int j = 0; j < rows; j++){
                result[i] += this.values[j][i];
            }
        }
        
        return new Array(result);
    }
    public float [] maxAxis() {
    	float [] result = new float [this.rows];
    	float max;
    	for (int i = 0 ; i< this.rows ; i++) {
    		max = 0;
    		for (int j = 0; j < this.columns ; j++) {
    		
    			if( this.values [i][j] > max) {
    				max = this.values [i][j];
    				result[i] = max;
    			}
    		}
    	}
    	return result;
    }
    public int [] IdxmaxAxis() {
    	int [] result = new int [this.rows];

    	int idxMax;
    	for (int i = 0 ; i< this.rows ; i++) {
    		idxMax = 0;
 
    		for (int j = 0; j < this.columns ; j++) {
    		
    			if( this.values [i][j] > this.values[i][idxMax]) {
    				idxMax = j;
    				result[i] = idxMax;
    			}
    		}
    	}
    	return result;
    }
    public Matrix sub(Matrix matrix2) throws Exception {
        if (this.rows != matrix2.rows || this.columns != matrix2.columns){
            throw new Exception("Cannot subtract matrices of different sizes");
        }
        
        float[][] result = new float[rows][columns];
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < columns; j++){
                result[i][j] = this.values[i][j] - matrix2.values[i][j];
            }
        }
        return new Matrix(result);
    }
    
    public Matrix Normalize() {

    	float [][] result = new float[rows][columns];
    	float [] maxList = new float [columns];
    	float [] minList = new float [columns];
    	for (int j = 0; j < columns; j++) {
    		for(int i = 0; i < rows; i++) {
    			if (this.values[i][j] > maxList[j]  ) {
    				maxList[j] = this.values[i][j];
    			if (this.values[i][j] < minList[j]  ) {
    				minList[j] = this.values[i][j];
    			}
    			}
    		}
    		
    	}
//    	System.out.println("maxList"+Arrays.toString(maxList));
//		System.out.println(Arrays.toString(minList));
    	for(int j = 0; j < columns; j++) {
    		for(int i = 0 ; i < rows ; i ++) {
    			result[i][j] = (this.values[i][j] - minList[j])/(maxList[j]-minList[j]);
    		}
    	}
    return new Matrix(result);
    }
    
    public Matrix add(Matrix matrix2) throws Exception {
        if (this.rows != matrix2.rows || this.columns != matrix2.columns) {
            throw new Exception("Cannot add matrices of different sizes");
        }
        
        float[][] result = new float[rows][columns];
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < columns; j++){
                result[i][j] = this.values[i][j] + matrix2.values[i][j];
            }
        }
        return new Matrix(result);
    }
    
    public Matrix add(float[] arr) throws Exception {
        if (this.columns != arr.length) {
            throw new Exception("Cannot add array of length " + arr.length + " with matrix with " + this.columns + " columns");
        }
        
        float[][] result = new float[rows][columns];
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < columns; j++){
                result[i][j] = this.values[i][j] + arr[j];
            }
        }
        return new Matrix(result);
    }
    
    public Matrix add(Array arr) throws Exception {
        return this.add(arr.values);
    }
    
    public Matrix mult(float scalar) {
        float[][] result = new float[rows][columns];
        
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < columns; j++){
                result[i][j] = values[i][j] * scalar;
            }
        }
        return new Matrix(result);
    }
    
    public Matrix mult(int scalar) {
        return mult((float) scalar);
    }
    
    public Matrix mult(Matrix matrix2) throws Exception {
        if (this.rows != matrix2.rows || this.columns != matrix2.columns) {
            throw new Exception("Cannot multiply matrices of different sizes");
        }
        
        float[][] result = new float[rows][columns];
        for (int i = 0; i < rows; i++){
            for (int j = 0; j < columns; j++){
                result[i][j] = this.values[i][j] * matrix2.values[i][j];
            }
        }
        return new Matrix(result);
    }
    
    public Matrix div(Matrix matrix2) throws Exception {
        if (this.rows != matrix2.rows) {
            throw new Exception("Incompatible matrices for division");
        }
        float[][] result;
        if (this.columns == matrix2.columns) {
            result = new float[rows][columns];
            for (int i = 0; i < rows; i++){
                for (int j = 0; j < columns; j++){
                    result[i][j] = this.values[i][j] / matrix2.values[i][j];
                }
            }
        } else if (matrix2.columns == 1) {
            result = new float[rows][columns];
            for (int i = 0; i < rows; i++){
                for (int j = 0; j < columns; j++){
                    result[i][j] = this.values[i][j] / matrix2.values[i][0];
                }
            }
        } else {
            throw new Exception("Incompatible matrices for division");
        }
        
        return new Matrix(result);
    }
    
    
    public Matrix matmul(Matrix matrix2) throws Exception {
        if(this.columns != matrix2.rows) {
            throw new Exception("Incompatible matrices for multiplication");
        }
        
        float[][] result = new float[this.rows][matrix2.columns];
        for(int i = 0; i < this.rows; i++) {         
            for(int j = 0; j < matrix2.columns; j++) {    
                for(int k = 0; k < this.columns; k++) { 
                    result[i][j] += this.values[i][k] * matrix2.values[k][j];
                }
            }
        }
        return new Matrix(result);
    }
    
    public Matrix dot(Matrix matrix2) {
        float [][] result = new float[rows][matrix2.columns];
    
        for (int i = 0; i < this.rows; i++) { 
            for (int j = 0; j < matrix2.columns; j++) { 
                for (int k = 0; k < this.columns; k++) { 
                    result[i][j] += this.values[i][k] * matrix2.values[k][j];
                }
            }
        }
        return new Matrix(result);
    }
    
    public Matrix log() {
        float [][] result = new float[rows][columns];
        
        for (int i = 0; i < this.rows; i++) { 
            for (int j = 0; j < this.columns; j++) { 
                result[i][j] += Math.log(this.values[i][j]);
            }
        }
        return new Matrix(result);
    }
    
    @Override
    public String toString() {
        String output = "[";
        for (int i = 0; i < rows; i++) {
            output += "[";
            for (int j = 0; j < columns; j++) {
                if (this.values[i][j] >= 0) { output += " "; }
                output += String.format("%.3f ", this.values[i][j]); 
            }
            if (i == rows-1) { output += "]"; }
            output += "]\n ";
        }
        return output;
    }
    
    
}

