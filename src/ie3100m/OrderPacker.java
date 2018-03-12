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
        
        ArrayList<PackingConfig> binsByNumbers = new ArrayList<>(configs);
        ArrayList<PackingConfig> binsByVolume = new ArrayList<>(configs);

        PackingConfig bestConfig = determineBestConfig(binsByNumbers, binsByVolume);

//        if (bestConfig == null) {
//            System.out.println("No suitable config found");
//        } else {
//            System.out.println("Chosen config:");
//            System.out.println(bestConfig);
//        }
        return bestConfig;
    }

    /**
     * Determine the best packing configuration based on the sorting order
     *
     * @param packingConfigs the array of possible packing configurations
     * @return the most desired packing configuration
     */
    private static PackingConfig determineBestConfig(ArrayList<PackingConfig> binsByNumbers, ArrayList<PackingConfig> binsByVolume) {
//        System.out.println("testing inside determine");
        Collections.sort(binsByNumbers, (a, b) -> {
            if (a.getTotalBinsInclRemainder() == b.getTotalBinsInclRemainder()) {
                return a.getTotalEmptyVol() <= b.getTotalEmptyVol() ? -1 : 1;
            } else {
                return a.getTotalBinsInclRemainder() - b.getTotalBinsInclRemainder();
            }
        });

        Collections.sort(binsByVolume, (a, b) -> {
            if (a.getMainBinStats().getEmptyVolume() == b.getMainBinStats().getEmptyVolume()) {
                return a.getTotalBinsInclRemainder() <= b.getTotalBinsInclRemainder() ? -1 : 1;
            } else {
                return a.getMainBinStats().getEmptyVolume() - b.getMainBinStats().getEmptyVolume();
            }
        });
//        System.out.println("sorted 2 arrays by num and vol");
        int rankPoints;
        ArrayList<RankSystem> rankBins = new ArrayList<>();

        for (int i = 0; i < binsByNumbers.size(); i++) {
            for (int j = 0; j < binsByVolume.size(); j++) {
                if (binsByNumbers.get(i).getMainBinStats().getBin().getName() == binsByVolume.get(j).getMainBinStats().getBin().getName()) {
                    rankPoints = i + j;
                    RankSystem rank = new RankSystem(binsByVolume.get(j), i, j, rankPoints);
                    rankBins.add(rank);
                }
            }

        }
        Collections.sort(rankBins);
        
        
//        for (RankSystem rank : rankBins) {
//            System.out.println(rank.toString());
//        }
        for (RankSystem rank : rankBins) {
            if (rank.getConfig().getMainBinStats().getTotalQuantity() != 1) {
                return rank.getConfig();
            }            
        }
        return null;
    }

    private static int calcUpperBound(Level2_Box box, Level3_Bin bin) {
        return bin.getBaseArea() / box.getBaseArea();
    }
}
