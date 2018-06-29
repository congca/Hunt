package no.uib.hunt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import no.uib.hunt.Test;
import no.uib.hunt.model.Variant;
import no.uib.hunt.utils.ProgressHandler;
import no.uib.hunt.vcf.GenotypeProvider;

/**
 * This script runs stress tests on the vcf access using a single vcf.
 *
 * @author Marc Vaudel
 */
public class SingleVcfTest extends Test {

    /**
     * Runs the tests.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            
            ProgressHandler.logFile = new File("SingleVcfTest.log");
            ProgressHandler.setUpLog();

            SingleVcfTest test = new SingleVcfTest(args[0]);
            test.initiatlize();
            test.runTests();
            test.close();
            
            ProgressHandler.close();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * The chromosome to use.
     */
    private final String chr;

    /**
     * Constructor.
     *
     * @param chr the chromosome to test
     */
    public SingleVcfTest(String chr) {

        super(new GenotypeProvider());

        this.chr = chr;

    }

    /**
     * Runs the tests.
     *
     * @throws IOException exception thrown if an error occurred while reading
     * or writing a file
     */
    public void runTests() throws IOException {

        final HashMap<String, ArrayList<Variant>> variantsMap = getTestVariants();
        ArrayList<Variant> variantList = variantsMap.get(chr);

        ProgressHandler progressHandler = new ProgressHandler();

        String task = "Testing Variant query on chromosome " + chr;
        progressHandler.start(task);

        try {

            testChromosome(chr, variantList);

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throwable.printStackTrace();
            ProgressHandler.writeToLog(throwable.toString());

        }
    }
}
