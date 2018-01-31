/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ie3100m;

import Model.Order;
import Model.Product.Level3_Bin;
import Model.Stats.PackingConfig;
import Utils.BinScanner;
import Utils.OrderScanner;
import java.io.IOException;
import java.util.ArrayList;
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
        
        ArrayList<PackingConfig> orderPacks = new ArrayList<>();
        
        /*
        1. Scan in list of orders
        2. Send order 1 by 1 into Orderpacker
        3. Orderpacker tell me the PackingConfig to use
        4. From all PackingConfig, find utilization rate of all bins
        */
        
        orderList.stream().forEach((order) -> {
            orderPacks.add(OrderPacker.packOrder(order));
        });
        
        ArrayList<Level3_Bin> binList = new ArrayList<>();
        try {
            binList = BinScanner.loadBinTypes("bins.csv");
        } catch (IOException ex) {
            Logger.getLogger(OrderPacker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList<BinAnalysis> bins = new ArrayList<>();
        
        for (Level3_Bin bin: binList) {
            for (PackingConfig order: orderPacks) {
            }
        }
    }
}
