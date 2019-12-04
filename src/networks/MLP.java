package networks;

import static utilities.Activations.sigmoid;
import static utilities.Activations.softmax;
import static utilities.Activations.sigmoidDerivative;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import robocode.RobocodeFileOutputStream;
import utilities.Array;
import utilities.Initializers.Init;
import utilities.Matrix;

public class MLP implements INetwork {
    
    public ArrayList<Matrix> w;
    private ArrayList<Array> b;
    private ArrayList<Matrix> out;
    
    private final float _lr;

    public MLP(int[] networkShape, float lr, Init initializer) {
        // weights
        w = new ArrayList<Matrix>();
        for (int i = 0; i < networkShape.length - 1; i++) {
            w.add(new Matrix(networkShape[i], networkShape[i+1], initializer));
        } 

        // bias
        b = new ArrayList<Array>();
        for (int i = 1; i < networkShape.length; i++) {
            b.add(new Array(networkShape[i], initializer));
        }

        // learning rate
        _lr = lr;

        // outputs from forward propagation
        out = new ArrayList<Matrix>();
    }
    
    public MLP(int[] networkShape, float lr) {
        this(networkShape, lr, Init.NORMAL);        
    }

    public Matrix forwardPropagate(Matrix x) {
        out.clear();
        	
        try {
            for (int i = 0; i < w.size(); i++) {
                // x is input for first layer
                if (i == 0) {
                    out.add(sigmoid(x.dot(w.get(i)).add(b.get(i))));
                } 

                else if (i == w.size() - 1) {
//              
                    out.add(out.get(i-1).dot(w.get(i)).add(b.get(i)));
                }
                else {
                    out.add(sigmoid(out.get(i-1).dot(w.get(i)).add(b.get(i))));
                }
            }
            return out.get(out.size()-1);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        
        
    }

    public void backPropagate(Matrix X, Matrix Y) {
        try {
            // calculate layer errors using derivative
            ArrayList<Matrix> deltas = new ArrayList<Matrix>();
            deltas.add(out.get(out.size()-1).sub(Y));
//            System.out.println("Printing from BP");
//            System.out.println(deltas.get(0));
            for (int i = out.size() - 1; i > 0; i--) {
                Matrix d = deltas.get(deltas.size() - 1);
                if (i == 1) {
                    deltas.add(d.dot(w.get(i).T()).mult(sigmoidDerivative(X.dot(w.get(i-1)).add(b.get(i-1)))));
                } else {
                    deltas.add(d.dot(w.get(i).T()).mult(sigmoidDerivative(out.get(i-2).dot(w.get(i-1)).add(b.get(i-1)))));
                }
            }
//            System.out.println("Deltas size");
//            System.out.println(deltas.size());
//            System.out.println("first layer" + deltas.get(0));
//            System.out.println("second layer" + deltas.get(1));
            // correct weights and biases using learning rate
            Matrix delta = deltas.get(deltas.size() - 1);
            w.set(0, w.get(0).sub(X.T().dot(delta).mult(_lr)));
            b.set(0, b.get(0).sub(delta.sumaxis().mult(_lr)));
            for (int i = 1; i < w.size(); i++) {
                delta = deltas.get(deltas.size() - i - 1);
                w.set(i, w.get(i).sub(out.get(i-1).T().dot(delta).mult(_lr)));
                b.set(i, b.get(i).sub(delta.sumaxis().mult(_lr)));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // checl loss...loss looks weird
    
    public float calcLoss(Matrix y) {
        double sum = 0;
        Matrix loss;

        try {
            loss = y.mult(-1).mult(out.get(out.size() - 1).log());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        for (int j=0; j < loss.rows; j++) {
            for (int k=0; k < loss.columns; k++) {
                sum += Math.pow((out.get(out.size() - 1).values[j][k]-y.values[j][k]),2);
            }
        }
        return (float) Math.sqrt(sum);
    }

    public void train(Matrix x, Matrix y, int epochs)  {
    	try {
			PrintWriter writer = new PrintWriter("training_results"+"-numHiddenLayer-2-numHiddenNeuronPerlayer-20"+_lr+".txt", "UTF-8");
		
        for (int i = 1; i <= epochs; i++) {
            forwardPropagate(x);
            backPropagate(x, y);
            
            if (i % 100 == 0) {
                System.out.format("Epoch %d - loss: %.3f\n", i, calcLoss(y));
                writer.println(calcLoss(y));
            }
//            if(calcLoss(y) < 0.05) {
//            	break;
//            }
        }
        writer.close();
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
        System.out.println();
    }
    
    public Matrix predict(Matrix x) {
        forwardPropagate(x);
        return out.get(out.size() - 1);
    }
}
