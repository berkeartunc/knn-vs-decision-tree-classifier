package com.mlproject.algorithms;

import com.mlproject.core.BaseAlgorithm;
import com.mlproject.data.UserRecord;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;


public class KNNClassifier extends BaseAlgorithm {

    private final int k;
    private List<UserRecord> trainingData;


    public KNNClassifier(int k) {
        this.k = k;
    }

    // KNN is a lazy learner: there is no real training step,
    // the training data is only stored and used at prediction time.
    @Override
    public void train(List<UserRecord> trainingData) {
        this.trainingData = trainingData;
    }

    @Override
    public String predict(UserRecord user) {
        if (trainingData == null || trainingData.isEmpty()) {
            return "UNKNOWN";
        }

        // A PriorityQueue ordered by distance keeps the closest neighbor at the front,
        // so the k nearest neighbors can be taken with poll().
        PriorityQueue<Neighbor> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

        // Distance from the given record to every training record
        for (UserRecord record : trainingData) {
            double dist = euclideanDistance(user.getFeatures(), record.getFeatures());
            queue.add(new Neighbor(record, dist));
        }

        // Voting: count the categories of the k nearest neighbors
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (int i = 0; i < k; i++) {
            if (queue.isEmpty()) break;
            
            Neighbor nearest = queue.poll(); // next closest neighbor
            String category = nearest.record.getCategory();

            // one more vote for this category
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        // The category with the most votes is the prediction
        String bestCategory = "UNKNOWN";
        int maxCount = -1;
        
        for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestCategory = entry.getKey();
            }
        }

        return bestCategory;
    }

    // Euclidean distance between two feature vectors:
    // square root of the sum of squared differences
    private double euclideanDistance(double[] features1, double[] features2) {
        double sum = 0.0;
        int length = Math.min(features1.length, features2.length);
        
        for (int i = 0; i < length; i++) {
            sum += Math.pow(features1[i] - features2[i], 2);
        }
        
        return Math.sqrt(sum);
    }

    // Small helper class that keeps a training record together with its distance
    private static class Neighbor {
        UserRecord record;
        double distance;

        Neighbor(UserRecord record, double distance) {
            this.record = record;
            this.distance = distance;
        }
    }
}
