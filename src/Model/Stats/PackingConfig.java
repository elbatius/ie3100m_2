/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model.Stats;

import Logic.BinStatsCalculator;
import Model.Order;
import Model.Product.Level3_Bin;


public class PackingConfig /*implements Comparable<PackingConfig>*/ {
    private final Order order;
    private final BinStats mainBinStats;
    

    private Level3_Bin lastBin;

    private long totalEmptyVol;
    private int totalBins;
    
    public PackingConfig (Order order, BinStats mainBinStats) {
        this.order = order;
        this.mainBinStats = mainBinStats;
    }
    
    public Order getOrder() {
        return this.order;
    }
    
    public BinStats getMainBinStats() {
        return this.mainBinStats;
    }

    public int getTotalBins() {
        return this.totalBins;
    }

    public int getTotalBinsInclRemainder() {
        return this.getRemainderBoxes() == 0 ? this.totalBins : this.totalBins + 1;
    }

    public int getRemainderBoxes() {
        return this.order.getQuantity() % this.mainBinStats.getTotalQuantity();
    }

    public Level3_Bin getLastBin() {
        return this.lastBin;
    }

    public long getTotalEmptyVol() {
        return this.totalEmptyVol;
    }
    
    public void setAttributes(Level3_Bin lastBin, int totalBins) {
        setLastBin(lastBin);
        setTotalBins(totalBins);
        
        if (lastBin == null) {//no remainder bins, either only 1 bin is needed or the boxes can fit into the main bins with no leftover
            if (totalBins == 1) { //case where only 1 bin is needed
                setTotalEmptyVol((long) this.mainBinStats.getBin().getTrimmedVolume(BinStatsCalculator.getBuffer(), BinStatsCalculator.getBufferBothSides()) - (this.order.getQuantity() * this.order.getBox().getVolume()));
            } else { //case where there are no leftovers
                setTotalEmptyVol((long) totalBins * this.mainBinStats.getEmptyVolume());
            }
        } else {
            setTotalEmptyVol(((long) totalBins * this.mainBinStats.getEmptyVolume()) + ((long) lastBin.getTrimmedVolume(BinStatsCalculator.getBuffer(), BinStatsCalculator.getBufferBothSides()) - (this.getRemainderBoxes() * this.order.getBox().getVolume())));
        }
    }
    
    private void setLastBin(Level3_Bin lastBin) {
        this.lastBin = lastBin;
    }
    
    private void setTotalBins(int totalBins) {
        this.totalBins = totalBins;
    }
    
    private void setTotalEmptyVol(long totalEmptyVol) {
//>>>>>>> origin/master
        this.totalEmptyVol = totalEmptyVol;
    }

    /**
     * Modify this method if another attribute is to be minimised
     * @param other
     * @return
     */
    /*@Override
    public int compareTo(PackingConfig other) {
        if (this.getTotalBinsInclRemainder() == other.getTotalBinsInclRemainder()) {
            return this.getTotalEmptyVol() <= other.getTotalEmptyVol() ? -1 : 1;
        } else {
            return this.getTotalBinsInclRemainder() - other.getTotalBinsInclRemainder();
        }
    }*/

    @Override
    public String toString() {

        String info = "Bin type: " + this.mainBinStats.getBin().toString() 
                + " Boxes per Bin: " + this.mainBinStats.getTotalQuantity()
                + " Bins Needed: " + totalBins + " empty vol: " + this.mainBinStats.getEmptyVolume();
        
        return info + (lastBin == null ? " Last Bin not needed" : "\n" + lastBin.toString() + " to contain remaining " + this.getRemainderBoxes() + " boxes");
    }

}
