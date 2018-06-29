package no.uib.hunt.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import no.uib.hunt.model.Variant;

/**
 * Pool of variants to use for testing.
 *
 * @author Marc Vaudel
 */
public class VariantPool {

    /**
     * Encoding for the file, cf the second rule.
     */
    public static final String encoding = "UTF-8";
    /**
     * File where variants are saved.
     */
    public static final String variantsFilePath = "resources/variants.gz";

    /**
     * Returns a map of variants to test indexed by chromosome.
     *
     * @return a map of variants to test indexed by chromosome
     *
     * @throws java.io.IOException exception thrown if an error occurred while
     * reading the variant file
     */
    public static HashMap<String, ArrayList<Variant>> getTestMap() throws IOException {

        HashMap<String, ArrayList<Variant>> variantMap = new HashMap<>();

        File variantsFile = new File(variantsFilePath);
        InputStream fileStream = new FileInputStream(variantsFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, encoding);

        try (BufferedReader br = new BufferedReader(decoder)) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split("\t");

                String rsId = lineSplit[1];
                String chr = lineSplit[2];
                String bpString = lineSplit[3];
                int bp = Integer.parseInt(bpString);
                Variant variant = new Variant(rsId, chr, bp);

                ArrayList<Variant> variantList = variantMap.get(chr);

                if (variantList == null) {

                    variantList = new ArrayList<>();
                    variantMap.put(chr, variantList);

                }

                variantList.add(variant);

            }

            return variantMap;

        }
    }
}
