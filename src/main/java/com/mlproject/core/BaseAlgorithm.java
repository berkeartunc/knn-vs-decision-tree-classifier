package com.mlproject.core;

import com.mlproject.data.UserRecord;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


// Abstract class and inheritance
// Implements IClassifier, but it is abstract because it is not an algorithm by itself.
// Keeps the shared evaluation code of KNN and Decision Tree in one place.

public abstract class BaseAlgorithm implements com.mlproject.core.IClassifier {

    // protected: only the algorithms that extend this class can access it,
    // other classes can't.
    // Confusion matrix: actual category -> predicted category -> count
    protected Map<String, Map<String, Integer>> confusionMatrix;

    // Calculates the accuracy (%) on the test data.
    // The same code is used for both KNN and Decision Tree.
    public double calculateAccuracy(List<UserRecord> testData) {
        if (testData == null || testData.isEmpty()) {
            return 0.0;
        }

        int correctPredictions = 0;
        confusionMatrix = new HashMap<>(); // reset the matrix for every test

        for (UserRecord record : testData) {
            String actualCategory = record.getCategory();


            // Polymorphism:
            // predict() is not implemented in this class.
            // At runtime, predict() of the actual object (KNN or Decision Tree) is called.
            String predictedCategory = predict(record);


            if (actualCategory != null && predictedCategory != null) {
                // Increase the count for this (actual, predicted) pair
                confusionMatrix.putIfAbsent(actualCategory, new HashMap<>());
                Map<String, Integer> row = confusionMatrix.get(actualCategory);
                row.put(predictedCategory, row.getOrDefault(predictedCategory, 0) + 1);
            }
            // Count the prediction as correct if it matches the actual category
            if (actualCategory != null && actualCategory.equals(predictedCategory)) {
                correctPredictions++;
            }
        }
        // Accuracy as a percentage
        return ((double) correctPredictions / testData.size()) * 100.0;
    }

    // Returns the confusion matrix as readable text (for console output)
    public String getConfusionMatrixReport() {
        if (confusionMatrix == null || confusionMatrix.isEmpty()) {
            return "Confusion matrix is not created yet.";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, Integer>> entry : confusionMatrix.entrySet()) {
            String rawActual = entry.getKey();
            Map<String, Integer> predictions = entry.getValue();

            for (Map.Entry<String, Integer> predEntry : predictions.entrySet()) {
                String rawPredicted = predEntry.getKey();
                int count = predEntry.getValue();


                String actual = (rawActual == null || rawActual.trim().isEmpty()) ? "[UNKNOWN]" : rawActual;
                String predicted = (rawPredicted == null || rawPredicted.trim().isEmpty()) ? "[UNKNOWN]" : rawPredicted;

                String status = actual.equals(predicted) ? "[Correct]" : "[Wrong]";
                sb.append(String.format("Actual: %-15s | Predicted: %-15s -> %3d times %s\n", 
                                        actual, predicted, count, status));
            }
        }
        return sb.toString();
    }
    // Returns the raw confusion matrix, used by the UI to draw its tables
    public Map<String, Map<String, Integer>> getRawConfusionMatrix() {
        return confusionMatrix;
    }
}
