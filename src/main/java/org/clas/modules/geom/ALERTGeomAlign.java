package org.clas.modules.geom;

/**
 * The {@code ALERTGeomAlign} class is used for geometric alignment 
 * for the ALERT detector system
 * 
 * @author npilleux
 */
public class ALERTGeomAlign {
    private double atof_bar_length = 279.7;
    private double atof_wedge_gap = 0.3;
    private double atof_wedge_length = 27.7;
    private double wedgeThickness = 20;
    private double barThickness = 3;
    private int numLayers = 4;
    private int numSectors = 15;
    private int numOrders = 2;
    private int numComponents = 10;
    
    /**
     * Gets the ATOF bar length.
     * @return the length of the ATOF bar in millimeters
     */
    public double getAtofBarLength() {
        return atof_bar_length;
    }

    /**
     * Sets the ATOF bar length.
     * @param atof_bar_length the length to set in millimeters
     */
    public void setAtofBarLength(double atof_bar_length) {
        this.atof_bar_length = atof_bar_length;
    }

    /**
     * Gets the ATOF wedge gap.
     * @return the gap between wedges in millimeters
     */
    public double getAtofWedgeGap() {
        return atof_wedge_gap;
    }

    /**
     * Sets the ATOF wedge gap.
     * @param atof_wedge_gap the gap to set in millimeters
     */
    public void setAtofWedgeGap(double atof_wedge_gap) {
        this.atof_wedge_gap = atof_wedge_gap;
    }

    /**
     * Gets the ATOF wedge length.
     * @return the length of the wedge in millimeters
     */
    public double getAtofWedgeLength() {
        return atof_wedge_length;
    }

    /**
     * Sets the ATOF wedge length.
     * @param atof_wedge_length the length to set in millimeters
     */
    public void setAtofWedgeLength(double atof_wedge_length) {
        this.atof_wedge_length = atof_wedge_length;
    }

    /**
     * Gets the thickness of the wedge.
     * @return the thickness of the wedge in millimeters
     */
    public double getWedgeThickness() {
        return wedgeThickness;
    }

    /**
     * Sets the thickness of the wedge.
     * @param wedgeThickness the thickness to set in millimeters
     */
    public void setWedgeThickness(double wedgeThickness) {
        this.wedgeThickness = wedgeThickness;
    }

    /**
     * Gets the thickness of the bar.
     * @return the thickness of the bar in millimeters
     */
    public double getBarThickness() {
        return barThickness;
    }

    /**
     * Sets the thickness of the bar.
     * @param barThickness the thickness to set in millimeters
     */
    public void setBarThickness(double barThickness) {
        this.barThickness = barThickness;
    }
    
     /**
     * Gets the number of layers in the detector.
     * @return the number of layers
     */
    public int getNumLayer() {
        return numLayers;
    }

    /**
     * Sets the number of layers in the detector.
     * @param numLayers the number of layers to set
     */
    public void setNumLayer(int numLayers) {
        this.numLayers = numLayers;
    }

    /**
     * Gets the number of sectors in the detector.
     * @return the number of sectors
     */
    public int getNumSector() {
        return numSectors;
    }

    /**
     * Sets the number of sectors in the detector.
     * @param numSectors the number of sectors to set
     */
    public void setNumSector(int numSectors) {
        this.numSectors = numSectors;
    }

    /**
     * Gets the number of orders.
     * @return the number of orders
     */
    public int getNumOrder() {
        return numOrders;
    }

    /**
     * Sets the number of orders.
     * @param numOrders the number of orders to set
     */
    public void setNumOrder(int numOrders) {
        this.numOrders = numOrders;
    }
    
    /**
     * Gets the number of components.
     * @return the number of components
     */
    public int getNumComponent() {
        return numComponents;
    }

    /**
     * Sets the number of orders.
     * @param numOrders the number of orders to set
     */
    public void setNumComponent(int numComponents) {
        this.numComponents = numComponents;
    }
}
