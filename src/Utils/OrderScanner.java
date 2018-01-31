/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Model.Order;
import Model.Product.Level2_Box;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author elbat
 */
public class OrderScanner {
    /**
     * Reads the CSV file with the given name and returns the list of Level3_Bin objects
     * @param fileName name of the file
     * @return the array of Level3_Bin objects
     * @throws IOException 
     */
    public static ArrayList<Order> loadOrders(String fileName) throws IOException {
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<Order> orders = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(fileName));
        while ((line = br.readLine()) != null) {
            // csv is a row
            String[] orderData = line.split(cvsSplitBy);
            orders.add(new Order(new Level2_Box(orderData[0], 
                    Integer.parseInt(orderData[1]), 
                    Integer.parseInt(orderData[2]), 
                    Integer.parseInt(orderData[3]), 0.06), 
                    Integer.parseInt(orderData[4])));
        }
            
        return orders;
    }
}
