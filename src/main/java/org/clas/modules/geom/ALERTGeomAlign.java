package org.clas.modules.geom;

/**
 * The {@code ALERTGeomAlign} class is used for geometric alignment 
 * for the ALERT detector system
 */
public class ALERTGeomAlign {

    //All these parameters will need to be read from CCDB!!!
    
    /** Length of ALERT in mm. */
    private double atof_bar_length = 279.7;

    /** Length of an ATOF wedge in mm. */
    private double atof_wedge_length = 27.7;

    /** Z gap between ATOF wedges in mm. */
    private double atof_wedge_gap = 0.3;

    /** Thickness of a bar in mm. */
    private double barThickness = 3.0;

    /** Thickness of a wedge in mm. */
    private double wedgeThickness = 20.0;
    
    /** Number of sectors. */
    private int numSector = 15;
    /** Number of layers. */
    private int numLayer = 4;
    /** Number of components. */
    private int numComp = 11;
    /** Number of orders. */
    private int numOrder = 2;
    
    /**
     * Returns the length of ALERT.
     *
     * @return the alert length in mm
     */
    public double getAtof_bar_length() {
        return atof_bar_length;
    }

    /**
     * Sets the length of ALERT.
     *
     * @param atof_bar_length the alert length in mm
     */
    public void setAtof_bar_length(double atof_bar_length) {
        this.atof_bar_length = atof_bar_length;
    }

    /**
     * Returns the length of an ATOF wedge.
     *
     * @return the ATOF wedge length in mm
     */
    public double getAtof_wedge_length() {
        return atof_wedge_length;
    }

    /**
     * Sets the length of an ATOF wedge.
     *
     * @param atof_wedge_length the ATOF wedge length in mm
     */
    public void setAtof_wedge_length(double atof_wedge_length) {
        this.atof_wedge_length = atof_wedge_length;
    }

    /**
     * Returns the gap between ATOF wedges.
     *
     * @return the ATOF wedge gap in mm
     */
    public double getAtof_wedge_gap() {
        return atof_wedge_gap;
    }

    /**
     * Sets the gap between ATOF wedges.
     *
     * @param atof_wedge_gap the ATOF wedge gap in mm
     */
    public void setAtof_wedge_gap(double atof_wedge_gap) {
        this.atof_wedge_gap = atof_wedge_gap;
    }

    /**
     * Returns the thickness of the bar.
     *
     * @return the bar thickness in mm
     */
    public double getBarThickness() {
        return barThickness;
    }

    /**
     * Sets the thickness of the bar.
     *
     * @param barThickness the bar thickness in mm
     */
    public void setBarThickness(double barThickness) {
        this.barThickness = barThickness;
    }
    
     /**
     * Returns the thickness of the bar.
     *
     * @return the bar thickness in mm
     */
    public double getWedgeThickness() {
        return wedgeThickness;
    }

    /**
     * Sets the thickness of the bar.
     *
     * @param barThickness the bar thickness in mm
     */
    public void setWedgeThickness(double wedgeThickness) {
        this.wedgeThickness = wedgeThickness;
    }
    
    /**
     * Gets the number of sectors.
     *
     * @return the number of sectors
     */
    public int getNumSector() {
        return numSector;
    }

    /**
     * Sets the number of sectors.
     *
     * @param numSector the new number of sectors
     */
    public void setNumSector(int numSector) {
        this.numSector = numSector;
    }

    /**
     * Gets the number of layers.
     *
     * @return the number of layers
     */
    public int getNumLayer() {
        return numLayer;
    }

    /**
     * Sets the number of layers.
     *
     * @param numLayer the new number of layers
     */
    public void setNumLayer(int numLayer) {
        this.numLayer = numLayer;
    }

    /**
     * Gets the number of components.
     *
     * @return the number of components
     */
    public int getNumComp() {
        return numComp;
    }

    /**
     * Sets the number of components.
     *
     * @param numComp the new number of components
     */
    public void setNumComp(int numComp) {
        this.numComp = numComp;
    }

    /**
     * Gets the number of orders.
     *
     * @return the number of orders
     */
    public int getNumOrder() {
        return numOrder;
    }

    /**
     * Sets the number of orders.
     *
     * @param numOrder the new number of orders
     */
    public void setNumOrder(int numOrder) {
        this.numOrder = numOrder;
    }
}

