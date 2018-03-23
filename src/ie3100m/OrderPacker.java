/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Logic.BinStatsCalculator;
import Logic.PackingConfigCalculator;
import Model.Stats.BinStats;
import Model.Product.Level2_Box;
import Model.Product.Level3_Bin;
import Model.Order;
import Model.Stats.ConfigObjective;
import Model.Stats.PackingConfig;
import Model.Stats.RankSystem;
import Utils.BinScanner;
import ilog.concert.IloException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kevin-Notebook
 */
public class OrderPacker {

    public static final double MAX_WEIGHT = 30;
    public static final double numCoeff = 0.25;
    public static final double volCoeff = 0.75;

    public static PackingConfig packOrder(Order order) {
        /**
         * TODO: put order stats as input
         */
        int numOrderedBox = order.getQuantity();
        Level2_Box box = new Level2_Box(order.getBox());
//        Order order = new Order(box, numOrderedBox); //in mm and g

        ArrayList<BinStats> allBinStats = new ArrayList<>();
        ArrayList<PackingConfig> configs = new ArrayList<>();
        
        BinStatsCalculator.updateBox(box);
        
        BinStatsCalculator.setStatsForAllBins();
        
        allBinStats.addAll(Arrays.asList(BinStatsCalculator.getBinStats()));

        for (BinStats binStat : allBinStats) {
            if (binStat.getTotalQuantity() > 0) {
                configs.add(new PackingConfig(order, binStat));
            }
        }
        PackingConfigCalculator.setAllConfigs(configs, allBinStats);

        PackingConfig bestConfig = determineBestConfig(configs, numCoeff, volCoeff);

//        if (bestConfig == null) {
//            System.out.println("No suitable config found");
//        } else {
//            System.out.println("Chosen config:");
//            System.out.println(bestConfig);
//        }
        return bestConfig;
    }

    /**
     * Determine the best packing configuration based on the objectives
     *
     * @param packingConfigs the array of possible packing configurations
     * @param numCoeff coefficient for number of bins in the configuration
     * @param volCoeff coefficient for the empty volume in the configuration
     * @return the most desired packing configuration
     */    
    private static PackingConfig determineBestConfig(ArrayList<PackingConfig> configs, double numCoeff, double volCoeff) {
        ArrayList<ConfigObjective> configObjectives = new ArrayList<>();
        int minNum = Integer.MAX_VALUE;
        long minVol = Long.MAX_VALUE;
        
        for (PackingConfig config : configs) {
            minNum = Math.min(minNum, config.getTotalBinsInclRemainder());
            minVol = Math.min(minVol, config.getTotalEmptyVol());
        }
        
        for (PackingConfig config : configs) {
            configObjectives.add(new ConfigObjective(config, (numCoeff * minNum / config.getTotalBinsInclRemainder()) + (volCoeff * minVol / config.getTotalEmptyVol())));
        }
        
        Collections.sort(configObjectives);
        
        for (ConfigObjective configObjective : configObjectives) {
            if (configObjective.getConfig().getOrder().getQuantity() == 1) {
                return configObjective.getConfig();
            } else if (configObjective.getConfig().getMainBinStats().getTotalQuantity() != 1) {
                return configObjective.getConfig();
            }            
        }
        
        return null;
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
