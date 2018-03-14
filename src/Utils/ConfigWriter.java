/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Model.Stats.PackingConfig;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elbat
 */
public class ConfigWriter {

    public static void saveConfig(ArrayList<PackingConfig> orderPacks) throws IOException {
        String csvFile = "packingconfigs.csv";
        FileWriter writer = new FileWriter(csvFile);

        List<String> header = new ArrayList<>();
        header.add("Order Quantity");
        header.add("Box Dimensions");
        header.add("Main Bin");
        header.add("Main Bin Dimensions");
        header.add("Main Bin Qty");
        header.add("Boxes per Bin");
        header.add("Empty Vol");
        header.add("Last Bin");
        header.add("Remainder boxes");

        CSVUtils.writeLine(writer, header);

        for (PackingConfig config : orderPacks) {
            List<String> list = new ArrayList<>();
            list.add(String.valueOf(config.getOrder().getQuantity()));
            list.add(config.getOrder().getBox().getDimensions());
            list.add(config.getMainBinStats().getBin().getFullName());
            list.add(config.getMainBinStats().getBin().getDimensions());
            list.add(String.valueOf(config.getTotalBins()));
            list.add(String.valueOf(config.getMainBinStats().getTotalQuantity()));
            list.add(String.valueOf(config.getMainBinStats().getEmptyVolume()));
            if (config.getLastBin() == null || config.getRemainderBoxes() == 0) {
                list.add("No remainder bin required");
                list.add(String.valueOf(0));
            } else {
                list.add(config.getLastBin().getFullName());
                list.add(String.valueOf(config.getRemainderBoxes()));
            }
            CSVUtils.writeLine(writer, list);
        }

        writer.flush();
        writer.close();

    }
}
