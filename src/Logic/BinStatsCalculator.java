/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Stats.BinStats;
import ilog.concert.IloException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * The class containing methods to determine the statistics for filling a Level 3 Bin full of Level 2 Boxes
 * @author Kevin-Notebook
 */
public class BinStatsCalculator {
    public static double MAX_WEIGHT = 30;
    public static Level3_Bin[] bins;
    public static BinStats[] binStats;
    public static Solver[] solvers;
    
    public void setWeight(double maxWeight) {
        MAX_WEIGHT = maxWeight;
    }
    
    public static void initComponents(ArrayList<Level3_Bin> bins) throws IloException {
        BinStatsCalculator.bins = bins.toArray(new Level3_Bin[0]);
        solvers = new Solver[bins.size()];
        
        for (int i = 0; i < bins.size(); i++) {
            solvers[i] = new Solver(bins.get(i));
        }
    }
    
    public static void updateBox(Level2_Box box) {
        binStats = new BinStats[bins.length];
        for (int i = 0; i < bins.length; i++) {
            binStats[i] = new BinStats(bins[i]);
            binStats[i].updateBox(box);
        }
    }
    
    /**
     * Sets the statistics for all the elements of a BinStats array.
     * @param allBinStats The array of BinStats whose attributes are to be calculated
     */
    public static void setStatsForAllBins() {
        IntStream.range(0, solvers.length).forEach(i -> {
            try {
                setStatsForBin(binStats[i], solvers[i]);
            } catch (IloException ex) {
                Logger.getLogger(BinStatsCalculator.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        return;
    }
    
    /**
     * Determines the maximum number of the specified level 2 boxes that can fit into the given level 3 bin.
     * @param binStats the BinStats whose attributes are to be determined
     * @throws IloException 
     */
    public static void setStatsForBin(BinStats binStats, Solver solver) throws IloException {
        Level2_Box box = binStats.getBox();
        Level3_Bin bin = binStats.getBin();
        
        solver.update(box, calcUpperBound(box, bin));
        
        int quantityPerLayer = solver.optimize(false);
        
        int totalQuantity = quantityPerLayer * (bin.getHeight() / box.getHeight());
        if (totalQuantity * box.getWeight() > MAX_WEIGHT) {
            totalQuantity = (int) (MAX_WEIGHT / box.getWeight());
        }
        
        binStats.setAttributes(quantityPerLayer, totalQuantity);
        solver.reset();
        
        return;
    }
    
    public static BinStats[] getBinStats() {
        return binStats;
    }
    
    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
