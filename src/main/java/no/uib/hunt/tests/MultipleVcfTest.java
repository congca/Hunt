package no.uib.hunt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.uib.hunt.Test;
import no.uib.hunt.model.Variant;
import no.uib.hunt.utils.ProgressHandler;
import no.uib.hunt.vcf.GenotypeProvider;

/**
 * This script runs stress tests on the vcf access.
 *
 * @author Marc Vaudel
 */
public class MultipleVcfTest extends Test {

    /**
     * Runs the tests.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            
            ProgressHandler.logFile = new File("MultipleVcfTest.log");
            ProgressHandler.setUpLog();

            MultipleVcfTest test = new MultipleVcfTest();
            test.initiatlize();
            test.runTests();
            test.close();
            
            ProgressHandler.close();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor.
     */
    public MultipleVcfTest() {

        super(new GenotypeProvider());

    }

    /**
     * Runs the tests.
     *
     * @throws IOException exception thrown if an error occurred while reading
     * or writing a file
     */
    public void runTests() throws IOException {

        final HashMap<String, ArrayList<Variant>> variantsMap = getTestVariants();

        ProgressHandler progressHandler = new ProgressHandler();

        String task = "Testing individual chromosomes";
        progressHandler.start(task);

        try {

            variantsMap.entrySet().stream()
                    .forEach(entry -> testChromosome(entry.getKey(), entry.getValue()));

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throwable.printStackTrace();
            ProgressHandler.writeToLog(throwable.toString());

        }

        TreeMap<String, Variant> variantsMapByRsid = variantsMap.values().stream()
                .flatMap(variantList -> variantList.stream())
                .collect(Collectors.toMap(
                        variant -> variant.rsId,
                        Function.identity(),
                        (a, b) -> {
                            throw new IllegalArgumentException("Non-unique rsid " + a.rsId);
                        },
                        TreeMap::new));

        task = "Testing all variants";
        progressHandler.start(task);

        try {

            testVariants(variantsMapByRsid);

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throwable.printStackTrace();
            ProgressHandler.writeToLog(throwable.toString());

        }
    }

    /**
     * Queries the variants in the given map.
     *
     * @param variantsMap a map of the variants ordered by rsid
     */
    public void testVariants(final TreeMap<String, Variant> variantsMap) {

        ProgressHandler progressHandler = new ProgressHandler();

        String task = "    Testing variants single threaded";
        progressHandler.start(task);

        try {

            variantsMap.values().stream()
                    .forEach(variant -> queryVariantSingleThread(variant));

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throwable.printStackTrace();
            ProgressHandler.writeToLog(throwable.toString());

        }

        task = "    Testing variants threaded per sample";
        progressHandler.start(task);

        try {

            variantsMap.values().stream()
                    .forEach(variant -> queryVariantThreadPerSample(variant));

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throwable.printStackTrace();
            ProgressHandler.writeToLog(throwable.toString());

        }

        task = "    Testing variants threaded per variant";
        progressHandler.start(task);

        try {

            variantsMap.values().parallelStream()
                    .forEach(variant -> queryVariantSingleThread(variant));

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throwable.printStackTrace();
            ProgressHandler.writeToLog(throwable.toString());

        }
    }

}
