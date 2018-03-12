/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

//testing this to push and learn how ot branch

import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;

import ilog.concert.*; //model
import ilog.cplex.*; //algo
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin-Notebook
 */
//kevin's branch
public class Solver {
    private IloCplex cplex;
    private int n; //upper bound of level 2 box
    
    private static final double M = 100000; //large integer (100K)
    
    private Level2_Box box;
    private Level3_Bin bin;
    
    private int boxVolume;
    private int binVolumes;
    
    //variables
    private IloIntVar[] P;

    //coordinates
    private IloIntVar[] x; //x_i
    private IloIntVar[] y; //y_i

    //orientation
    private IloIntVar[][] leftOf; //a_ik
    private IloIntVar[][] frontOf; //c_ik

    //alignment
    private IloIntVar[] isHorizontal; //l_xi
    
    private IloObjective objective;
    
    private IloConstraint[] boxConstraints;
    private IloConstraint[] comparingConstraints;
    private IloConstraint totalBoxConstraint;
    private IloConstraint[] binConstraints;
    private IloConstraint weightConstraint;
    
    public Solver() {
        try {
            this.cplex = new IloCplex();
        } catch (IloException ex) {
            Logger.getLogger(Solver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Solver(Level2_Box box, int n, Level3_Bin bin) throws IloException {
        this.cplex = new IloCplex();
        
        this.box = box;
        this.bin = bin;
        this.n = n;
        
        this.boxVolume = box.getVolume();
        this.binVolumes = bin.getVolume();
    }
    
    public Solver(Level3_Bin bin) throws IloException {
        this.cplex = new IloCplex();
        
        this.bin = bin;
        
        this.binVolumes = bin.getVolume();
    }
    
    public void update(Level2_Box box, int n) throws IloException {
        this.box = box;
        this.n = n;
        
        initVariables();
    }
    
    public int optimize(boolean output) throws IloException {
        if (!output) {
            cplex.setOut(null);
        }

        objective = defineObjective();
        
        //constraints
        //Lvl 2 box spatial constraints
        boxConstraints = addBoxOverlappingConstraints();
        
        //Comparing box constraints
        comparingConstraints = addComparingConstraints();
        
        totalBoxConstraint = cplex.addLe(cplex.sum(P), n);
        
        //Lvl 3 Bin spatial constraints
        binConstraints = addBinOverlappingConstraints();
        
        //Weight constraints
        IloLinearNumExpr XsumWeight = cplex.linearNumExpr();
        for (int i = 0; i < n; i++) {
            XsumWeight.addTerm(box.getWeight(), P[i]);
        }
        weightConstraint = cplex.addLe(XsumWeight, 30);
        
        if (cplex.solve()) {
            if (output) {
                System.out.println("Free Space: " + (bin.getVolume() - box.getVolume() * cplex.getObjValue()));
                System.out.println("Number of boxes: " + cplex.getObjValue());

                for (int i = 0; i < n; i++) {
                    if (cplex.getValue(P[i]) > 0) {
                        System.out.println(String.format("(%d, %d)", Math.round(cplex.getValue(x[i])), Math.round(cplex.getValue(y[i]))));
                    }
                }
            }
            return (int) Math.round(cplex.getObjValue());
        } else {
            System.out.println("Solution not found.");
            return Integer.MAX_VALUE;
        }
    }
    
    public void initVariables() throws IloException {
        P = cplex.boolVarArray(n);

        x = cplex.intVarArray(n, 0, Integer.MAX_VALUE); //x_i
        y = cplex.intVarArray(n, 0, Integer.MAX_VALUE); //y_i
        
        leftOf = new IloIntVar[n][n]; //a_ik
        frontOf = new IloIntVar[n][n]; //c_ik
        
        for (int i = 0; i < n; i++) {
            leftOf[i] = cplex.boolVarArray(n);
            frontOf[i] = cplex.boolVarArray(n);
        }
        
        isHorizontal = cplex.boolVarArray(n); //l_xi
    }
    
    private IloObjective defineObjective() throws IloException {
        return cplex.addMaximize(cplex.sum(P));
    }
    
    private IloConstraint[] addBoxOverlappingConstraints() throws IloException {
        ArrayList<IloConstraint> constraints = new ArrayList<>(n * n * 2);
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (i == k) continue;
                constraints.add(cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(x[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, leftOf[i][k]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[k]))))));
                constraints.add(cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))),
                        cplex.sum(y[k], cplex.prod(M, cplex.sum(1, cplex.prod(-1, frontOf[i][k]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i]))), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[k]))))));
            }
        }
        
        return constraints.toArray(new IloConstraint[0]);
    }
    
    private IloConstraint[] addComparingConstraints() throws IloException {
        ArrayList<IloConstraint> constraints = new ArrayList<>((3 * n * (n+1)) / 2);
        
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < k; i++) {
                constraints.add(cplex.addGe(cplex.sum(leftOf[i][k], frontOf[i][k]), cplex.sum(cplex.sum(P[i], P[k]), -1)));
                constraints.add(cplex.addLe(cplex.sum(leftOf[i][k], frontOf[i][k]), P[i]));
                constraints.add(cplex.addLe(cplex.sum(leftOf[i][k], frontOf[i][k]), P[k]));
            }
        }
        
        return constraints.toArray(new IloConstraint[0]);
    }
    
    private IloConstraint[] addBinOverlappingConstraints() throws IloException {
        ArrayList<IloConstraint> constraints = new ArrayList<>(2 * n);
        
        for (int i = 0; i < n; i++) {
            constraints.add(cplex.addLe(cplex.sum(x[i], cplex.prod(box.getLength(), isHorizontal[i]), cplex.prod(box.getWidth(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    cplex.sum(bin.getLength(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i]))))));
            constraints.add(cplex.addLe(cplex.sum(y[i], cplex.prod(box.getWidth(), isHorizontal[i]), cplex.prod(box.getLength(), cplex.sum(1, cplex.prod(-1, isHorizontal[i])))), 
                    cplex.sum(bin.getWidth(), cplex.prod(M, cplex.sum(1, cplex.prod(-1, P[i]))))));
        }
        
        return constraints.toArray(new IloConstraint[0]);
    }
    
    public void reset() throws IloException {
        cplex.remove(objective);
        cplex.remove(boxConstraints);
        cplex.remove(comparingConstraints);
        cplex.remove(totalBoxConstraint);
        cplex.remove(binConstraints);
        cplex.remove(weightConstraint);
    }
}
