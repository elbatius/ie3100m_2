/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Logic;

import Model.Product.Level3_Bin;
import Model.Stats.PackingConfig;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author elbat
 */
public class BinDimOptimizer {

    ArrayList<Level3_Bin> binList;
    ArrayList<PackingConfig> orderPacks;
    HashMap<String, Integer> binMap;

    public BinDimOptimizer(ArrayList<Level3_Bin> binList, ArrayList<PackingConfig> orderPacks, HashMap<String, Integer> binMap) {
        this.binList = binList;
        this.orderPacks = orderPacks;
        this.binMap = binMap;
    }

    public void optimizeHeight() {
        for (Level3_Bin bin : binList) {
            int maxHeight = 0;
            for (PackingConfig config : orderPacks) {
                if (config.getMainBinStats().getBin().getName() == bin.getName()) {
                    int qty;
                    if (config.getOrder().getQuantity() < config.getMainBinStats().getTotalQuantity()){
                        qty = config.getOrder().getQuantity();
                    } else {
                        qty = config.getMainBinStats().getTotalQuantity();
                    }
                    int layernum = qty / config.getMainBinStats().getQuantityPerLayer();
                    if (layernum == 0) {
                        layernum = 1;
                    }
                    int height = config.getOrder().getBox().getHeight() * layernum;
                    if (height > maxHeight) {
                        maxHeight = height;
                    }
                }
            }
            if (maxHeight != 0) {
                maxHeight = ((maxHeight + 10) / 10) * 10;
            }
            if (bin.getHeight() != maxHeight && maxHeight != 0) {
                System.out.println("bin type: " + bin.getFullName() + " prev height: " + bin.getHeight());
                bin.setHeight(maxHeight);
                System.out.println("bin type: " + bin.getFullName() + " new height: " + bin.getHeight());
            }
        }
    }

}
