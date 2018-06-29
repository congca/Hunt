package no.uib.hunt.model;

/**
 * This class represents a marker that can be used to calculate a GRS.
 *
 * @author Marc Vaudel
 */
public class Variant {
    
    /**
     * The marker rs id.
     */
    public final String rsId;
    /**
     * The chromosome number.
     */
    public final String chr;
    /**
     * The base pair number.
     */
    public final int bp;
    
    /**
     * Constructor.
     * 
     * @param rsId The marker rs id
     * @param chr the chromosome number
     * @param bp the base pair number
     */
    public Variant(String rsId, String chr, int bp) {
        
        this.rsId = rsId;
        this.chr = chr;
        this.bp = bp;
        
    }

}
