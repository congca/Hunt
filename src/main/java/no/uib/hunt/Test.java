package no.uib.hunt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.uib.hunt.data.VariantPool;
import no.uib.hunt.model.Variant;
import no.uib.hunt.utils.ProgressHandler;
import no.uib.hunt.vcf.GenotypeProvider;

/**
 * This class tests the query of variants.
 *
 * @author Marc Vaudel
 */
public abstract class Test {

    /**
     * Array of all chromosomes.
     */
    public static final String[] chromosomes = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X"};
    /**
     * The folder containing the vcf files.
     */
    public static final String vcfFolder = "/media/local-disk2/marc/tmp/";
    /**
     * The number of sample values to test.
     */
    public static final int nSamples = 10;
    /**
     * The genotype provider to use to query the vcf files.
     */
    protected final GenotypeProvider genotypeProvider;

    /**
     * Constructor.
     *
     * @param genotypeProvider a genotype provider.
     */
    public Test(GenotypeProvider genotypeProvider) {

        this.genotypeProvider = genotypeProvider;

    }

    /**
     * Initializes the genotype provider using the vcf files in the vcfFolder
     * folder.
     */
    public void initiatlize() {

        ProgressHandler progressHandler = new ProgressHandler();

        String task = "Importing vcf files";
        progressHandler.start(task);

        Arrays.stream(chromosomes)
                .forEach(chr -> genotypeProvider.addVcfFile(
                chr,
                getVcfFilePath(chr),
                getIndexFilePath(chr)));

        progressHandler.end(task);

    }

    /**
     * Returns the map of variants to test.
     *
     * @return a map of variants to test
     *
     * @throws IOException exception thrown if an error occurs while parsing the
     * variant file.
     */
    public HashMap<String, ArrayList<Variant>> getTestVariants() throws IOException {

        ProgressHandler progressHandler = new ProgressHandler();

        String task = "Importing variants";
        progressHandler.start(task);

        try {

            HashMap<String, ArrayList<Variant>> variantsMap = VariantPool.getTestMap();

            progressHandler.end(task);

            return variantsMap;

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throw (throwable);

        }
    }

    /**
     * Queries the variants for this chromosome.
     *
     * @param chr the chromosome of interest
     * @param variantsList the variants list
     */
    public void testChromosome(String chr, ArrayList<Variant> variantsList) {

        ProgressHandler progressHandler = new ProgressHandler();

        String task = "    Testing " + chr + " single threaded";
        progressHandler.start(task);

        try {

            queryVariantsSingleThread(variantsList);

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throw (throwable);

        }

        task = "    Testing " + chr + " threaded per sample";
        progressHandler.start(task);

        try {

            queryVariantsThreadPerSample(variantsList);

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throw (throwable);

        }

        task = "    Testing " + chr + " threaded per variant";
        progressHandler.start(task);

        try {

            queryVariantsThreadPerVariant(variantsList);

            progressHandler.end(task);

        } catch (Throwable throwable) {

            progressHandler.writeLine(task + " failed.");

            throw (throwable);

        }
    }

    /**
     * Queries the given variant using a single thread.
     *
     * @param variant a variant to query.
     */
    public void queryVariantSingleThread(final Variant variant) {

        List<String> samples = sample(genotypeProvider.getSamples());

        samples.stream()
                .forEach(sample -> genotypeProvider.getGenotype(sample, variant));

    }

    /**
     * Queries the given variant using a single thread.
     *
     * @param variant a variant to query.
     */
    public void queryVariantThreadPerSample(final Variant variant) {

        List<String> samples = sample(genotypeProvider.getSamples());

        Spliterator<String> mainIt = samples.spliterator();

        List<Spliterator<String>> its = getSampleIterators(mainIt);
        its.parallelStream().forEach(it -> {

            GenotypeProvider threadGenotypeProvider = new GenotypeProvider();
            Arrays.stream(chromosomes)
                    .forEach(chr -> threadGenotypeProvider.addVcfFile(
                    chr,
                    getVcfFilePath(chr),
                    getIndexFilePath(chr)));

            it.forEachRemaining(sample -> threadGenotypeProvider.getGenotype(sample, variant));

        });
    }

    /**
     * Queries the given variants using a single thread.
     *
     * @param variants a list of variants to query.
     */
    public void queryVariantsSingleThread(ArrayList<Variant> variants) {

        List<String> samples = sample(genotypeProvider.getSamples());

        variants.stream()
                .forEach(variant -> samples.stream()
                .forEach(sample -> genotypeProvider.getGenotype(sample, variant)));

    }

    /**
     * Queries the given variants using a single thread.
     *
     * @param variants a list of variants to query.
     */
    public void queryVariantsThreadPerVariant(ArrayList<Variant> variants) {

        List<String> samples = sample(genotypeProvider.getSamples());

        Spliterator<Variant> mainIt = variants.spliterator();

        List<Spliterator<Variant>> its = getVariantIterators(mainIt);
        its.parallelStream().forEach(it -> {

            GenotypeProvider threadGenotypeProvider = new GenotypeProvider();
            Arrays.stream(chromosomes)
                    .forEach(chr -> threadGenotypeProvider.addVcfFile(
                    chr,
                    getVcfFilePath(chr),
                    getIndexFilePath(chr)));

            it.forEachRemaining(variant -> samples.stream()
                    .forEach(sample -> threadGenotypeProvider.getGenotype(sample, variant)));

        });

    }

    /**
     * Queries the given variants using a single thread.
     *
     * @param variants a list of variants to query.
     */
    public void queryVariantsThreadPerSample(ArrayList<Variant> variants) {

        List<String> samples = sample(genotypeProvider.getSamples());

        Spliterator<String> mainIt = samples.spliterator();

        List<Spliterator<String>> its = getSampleIterators(mainIt);
        its.parallelStream().forEach(it -> {

            GenotypeProvider threadGenotypeProvider = new GenotypeProvider();
            Arrays.stream(chromosomes)
                    .forEach(chr -> threadGenotypeProvider.addVcfFile(
                    chr,
                    getVcfFilePath(chr),
                    getIndexFilePath(chr)));

            it.forEachRemaining(sample -> variants.stream()
                    .forEach(variant -> threadGenotypeProvider.getGenotype(sample, variant)));

        });

    }

    /**
     * Returns the vcf file for a given chromosome.
     *
     * @param chromosome the chromosome of interest
     *
     * @return the vcf file
     */
    public static File getVcfFilePath(String chromosome) {
        return new File(String.join("", vcfFolder, chromosome, ".vcf.gz"));
    }

    /**
     * Returns the vcf index file for a given chromosome.
     *
     * @param chromosome the chromosome of interest
     *
     * @return the vcf index file
     */
    public static File getIndexFilePath(String chromosome) {
        return new File(String.join("", vcfFolder, chromosome, ".vcf.gz.tbi"));
    }

    /**
     * Closes the vcf files.
     */
    public void close() {
        genotypeProvider.close();
    }

    /**
     * Returns a list of length nSamples of sampled values from the given list.
     *
     * @param originalList the original list
     *
     * @return a list of length nSamples of sampled values from the given list
     */
    public List<String> sample(List<String> originalList) {

        return IntStream.range(0, nSamples)
                .map(i -> (int) Math.floor((originalList.size() - 1) * Math.random()))
                .mapToObj(i -> originalList.get(i))
                .collect(Collectors.toList());

    }

    /**
     * Returns a list of iterators corresponding to an attempt at splitting this
     * iterator in 16.
     *
     * @param iterator the original iterator
     *
     * @return a list of iterators.
     */
    private List<Spliterator<String>> getSampleIterators(Spliterator<String> iterator) {

        List<Spliterator<String>> iteratorList = new ArrayList<>(1);
        iteratorList.add(iterator);

        for (int i = 0; i < 4; i++) {

            List<Spliterator<String>> newList = new ArrayList<>(2 * iteratorList.size());

            for (Spliterator<String> it : iteratorList) {

                newList.add(it);

                Spliterator<String> newIt = it.trySplit();

                if (newIt != null) {

                    newList.add(newIt);

                }
            }

            iteratorList = newList;

        }

        return iteratorList;

    }

    /**
     * Returns a list of iterators corresponding to an attempt at splitting this
     * iterator in 16.
     *
     * @param iterator the original iterator
     *
     * @return a list of iterators.
     */
    private List<Spliterator<Variant>> getVariantIterators(Spliterator<Variant> iterator) {

        List<Spliterator<Variant>> iteratorList = new ArrayList<>(1);
        iteratorList.add(iterator);

        for (int i = 0; i < 4; i++) {

            List<Spliterator<Variant>> newList = new ArrayList<>(2 * iteratorList.size());

            for (Spliterator<Variant> it : iteratorList) {

                newList.add(it);

                Spliterator<Variant> newIt = it.trySplit();

                if (newIt != null) {

                    newList.add(newIt);

                }
            }

            iteratorList = newList;

        }

        return iteratorList;

    }
}
