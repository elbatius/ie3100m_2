/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Model.Order;
import Model.Product.Level3_Bin;
import Model.Stats.BinStats;
import Model.Stats.PackingConfig;
import Utils.BinScanner;
import Utils.OrderScanner;
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
        
        ArrayList<Level3_Bin> binList = new ArrayList<>();
        
        try {
            binList = BinScanner.loadBinTypes("bins.csv");
        } catch (IOException ex) {
            Logger.getLogger(OrderPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<PackingConfig> orderPacks = new ArrayList<>();
        
        /*
        orderPacks have configured packs inside. 
        */
        int j = 0;
        orderList.stream().forEach((order) -> {
            orderPacks.add(OrderPacker.packOrder(order));
            
            System.out.println("config done");
        });
            System.out.println("all config done");
        
        HashMap<String, Integer> binMap = new HashMap<String, Integer>();
        
        for (Level3_Bin bin: binList) {
            binMap.put(bin.getName(), 0);
        }
        int i = 0;
        for (PackingConfig config: orderPacks) {
            String key = config.getMainBinStats().getBin().getName();
            binMap.put(key, binMap.get(key)+1);
            i++;
            System.out.println("mapping done up till n = " + i);
        }
        
        
        
        
    }
}
