package no.uib.hunt.vcf;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import no.uib.hunt.model.Variant;

/**
 * Genotype provider based on vcf files.
 *
 * @author Marc Vaudel
 */
public class GenotypeProvider {

    /**
     * Map of all available vcf files indexed by chromosome name.
     */
    private final HashMap<String, VCFFileReader> vcfFiles = new HashMap<>();

    /**
     * Constructor.
     */
    public GenotypeProvider() {

    }
    
    /**
     * Returns the list of samples in the cohort.
     * 
     * @return the list of samples in the cohort
     */
    public List<String> getSamples() {
        
        VCFFileReader vcfFileReader = vcfFiles.values().stream().findAny().orElse(null);
        
        return vcfFileReader == null ? new ArrayList<>(0) : vcfFileReader.getFileHeader().getSampleNamesInOrder();
        
    }

    /**
     * Adds a vcf file.
     * 
     * @param chr the chromosome in this vcf file.
     * @param vcfFile the vcf file
     * @param indexFile the tbi index file
     */
    public void addVcfFile(String chr, File vcfFile, File indexFile) {

        VCFFileReader vcfFileReader = new VCFFileReader(vcfFile, indexFile);
        vcfFiles.put(chr, vcfFileReader);

    }
    
    /**
     * Returns the variant context for a variant.
     * 
     * @param variant the variant of interest
     * 
     * @return the variant context
     */
    public VariantContext getVariantContext(Variant variant) {

        VCFFileReader vcfFileReader = vcfFiles.get(variant.chr);

        try (CloseableIterator<VariantContext> iterator = vcfFileReader.query(variant.chr, variant.bp, variant.bp)) {

            while (iterator.hasNext()) {

                VariantContext variantContext = iterator.next();

                if (variantContext.getID().equals(variant.rsId)) {

                    return variantContext;

                }
            }
        }
        
        return null;
        
    }

    /**
     * Returns the genotype for a given variant in a given sample.
     * 
     * @param sample the sample of interest
     * @param variant the variant of interest
     * 
     * @return the genotype for a given variant in a given sample
     */
    public Genotype getGenotype(String sample, Variant variant) {
        
        VariantContext variantContext = getVariantContext(variant);
        
        return variantContext == null ? null : variantContext.getGenotype(sample);

    }
    
    /**
     * Closes all vcf files.
     */
    public void close() {
        
        vcfFiles.values().stream()
                .forEach(fileReader -> fileReader.close());
        
    }

}
