/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Logic.BinStatsCalculator;
import Model.Order;
import Model.Product.Level3_Bin;
import Model.Stats.BinStats;
import Model.Stats.PackingConfig;
import Utils.BinScanner;
import Utils.OrderScanner;
import ilog.concert.IloException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elbat
 */
public class Main {
        
        
        public static void main(String[] args) {
            
        ArrayList<Order> orderList = new ArrayList<>();
        try {
            orderList = OrderScanner.loadOrders("orders.csv");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int numOfOrders = 0;
        numOfOrders = orderList.size();
        
        ArrayList<Level3_Bin> binList = new ArrayList<>();
        
        try {
            binList = BinScanner.loadBinTypes("bins.csv");
            BinStatsCalculator.initComponents(binList);
        } catch (IOException ex) {
            Logger.getLogger(OrderPacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IloException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<PackingConfig> orderPacks = new ArrayList<>();
        
        /*
        orderPacks have configured packs inside. 
        */
        HashMap<String, Integer> binMap = new HashMap<String, Integer>();
        
        for (Level3_Bin bin: binList) {
            binMap.put(bin.getName(), 0);
        }
        
        int j = 0;
        for (Order order: orderList) {
            PackingConfig config = OrderPacker.packOrder(order);
            String key = config.getMainBinStats().getBin().getName();
            binMap.put(key, binMap.get(key)+1);
            System.out.println(j);
            j++;
        }
        
//        orderList.stream().forEach((order) -> {
//            PackingConfig config = OrderPacker.packOrder(order);
//            String key = config.getMainBinStats().getBin().getName();
//            binMap.put(key, binMap.get(key)+1);
//        });
        System.out.println("all config done");
        
        for (Level3_Bin bin: binList) {
            int count = binMap.get(bin.getName());
            String binName = bin.getName();
            float rate = (count * 100.0f)/numOfOrders;
//            System.out.println("count for " + binName + ": " + count);
            System.out.println("utilization rate for " + binName + ": " + rate);
        }
        
        
        
        
        
        
    }
}
