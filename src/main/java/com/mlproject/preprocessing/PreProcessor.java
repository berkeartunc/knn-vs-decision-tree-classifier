package com.mlproject.preprocessing;

import com.mlproject.data.UserRecord;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PreProcessor {

    // Label encoding for brands:
    // every new brand gets the next id (0, 1, 2, ...)
    private Map<String, Double> brandMap = new HashMap<>();
    private double nextBrandId = 0.0;

    // Min and max values for min-max normalization (learned from the training data).
    // Min starts at the largest double and max at the smallest,
    // so the first record always updates them.
    private double minAmount = Double.MAX_VALUE, maxAmount = -Double.MAX_VALUE;
    private double minPrice  = Double.MAX_VALUE, maxPrice  = -Double.MAX_VALUE;
    private double minTotal  = Double.MAX_VALUE, maxTotal  = -Double.MAX_VALUE;
    private double amountRange = 1.0;
    private double priceRange = 1.0;
    private double totalRange = 1.0;

    // Use only on the training data:
    // learns the min-max values (fit) and transforms the data
    public void fitAndTransform(List<UserRecord> trainData) {
        if (trainData == null || trainData.isEmpty()) return;

        cleanData(trainData); // remove records with missing values
        if (trainData.isEmpty()) return;

        Iterator<UserRecord> it = trainData.iterator();
        while (it.hasNext()) {
            UserRecord r = it.next();
            // Encode gender as 0 or 1, remove the record if the value is unknown
            if (!applyGender(r)) {
                it.remove();
                continue;
            }
            // Convert the brand to its id and store it in features[4]
            String brand = r.getBrand().trim().toUpperCase(Locale.ROOT);
            if (!brandMap.containsKey(brand)) {
                brandMap.put(brand, nextBrandId++);
            }
            r.getFeatures()[4] = brandMap.get(brand);
        }

        if (trainData.isEmpty()) return;

        // Find the min and max values in the training data.
        // Reset first, so calling this method again gives the same result
        minAmount = Double.MAX_VALUE; maxAmount = -Double.MAX_VALUE;
        minPrice  = Double.MAX_VALUE; maxPrice  = -Double.MAX_VALUE;
        minTotal  = Double.MAX_VALUE; maxTotal  = -Double.MAX_VALUE;

        for (UserRecord r : trainData) {
            minAmount = Math.min(minAmount, r.getAmount());
            maxAmount = Math.max(maxAmount, r.getAmount());
            minPrice  = Math.min(minPrice,  r.getPrice());
            maxPrice  = Math.max(maxPrice,  r.getPrice());
            minTotal  = Math.min(minTotal,  r.getLineNetTotal());
            maxTotal  = Math.max(maxTotal,  r.getLineNetTotal());
        }

        amountRange = (maxAmount - minAmount == 0) ? 1.0 : (maxAmount - minAmount);
        priceRange  = (maxPrice  - minPrice  == 0) ? 1.0 : (maxPrice  - minPrice);
        totalRange  = (maxTotal  - minTotal  == 0) ? 1.0 : (maxTotal  - minTotal);


        applyMinMax(trainData);
    }

    // Use only on the test data:
    // transforms it with the min-max values learned from the training data
    public void transform(List<UserRecord> testData) {
        if (testData == null || testData.isEmpty()) return;

        cleanData(testData);
        if (testData.isEmpty()) return;

        Iterator<UserRecord> it = testData.iterator();
        while (it.hasNext()) {
            UserRecord r = it.next();
            if (!applyGender(r)) {
                it.remove();
                continue;
            }
            String brand = r.getBrand().trim().toUpperCase(Locale.ROOT);
            if (!brandMap.containsKey(brand)) {

                brandMap.put(brand, nextBrandId++);
            }
            r.getFeatures()[4] = brandMap.get(brand);
        }


        applyMinMax(testData);
    }
    // Removes records with a null or empty category, gender or brand
    // (they would cause a NullPointerException later)
    private void cleanData(List<UserRecord> data) {
        data.removeIf(r ->
            r.getCategory() == null || r.getCategory().trim().isEmpty() ||
            r.getGender()   == null || r.getGender().trim().isEmpty()   ||
            r.getBrand()    == null || r.getBrand().trim().isEmpty()
        );
    }

    // Gender to binary: K/KADIN/F/FEMALE -> 0, E/ERKEK/M/MALE -> 1
    // Returns false if the value is not recognized
    private boolean applyGender(UserRecord r) {
        // Locale.ROOT: on a Turkish system "kadin".toUpperCase() would give "KADİN" (dotted İ)
        String g = r.getGender().trim().toUpperCase(Locale.ROOT);
        if (g.equals("K") || g.equals("KADIN") || g.equals("F") || g.equals("FEMALE")) {
            r.getFeatures()[0] = 0.0;
            return true;
        } else if (g.equals("E") || g.equals("ERKEK") || g.equals("M") || g.equals("MALE")) {
            r.getFeatures()[0] = 1.0;
            return true;
        }
        return false;
    }

    // Min-max normalization: (x - min) / (max - min)
    // Training values end up between 0 and 1. The brand id (features[4]) is not normalized.
    private void applyMinMax(List<UserRecord> data) {
        for (UserRecord r : data) {
            r.getFeatures()[1] = (r.getAmount() - minAmount) / amountRange;
            r.getFeatures()[2] = (r.getPrice()  - minPrice)  / priceRange;
            r.getFeatures()[3] = (r.getLineNetTotal() - minTotal)  / totalRange;
        }
    }
}
