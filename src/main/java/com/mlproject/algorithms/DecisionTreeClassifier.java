package com.mlproject.algorithms;

import com.mlproject.core.BaseAlgorithm;
import com.mlproject.data.UserRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DecisionTreeClassifier extends BaseAlgorithm {


    private final int maxDepth;
    private Node root;

    // A node of the tree:
    // either a split rule or a leaf with a category.
    private static class Node {
        String leafCategory; // category if this is a leaf
        int featureIndex; // which feature is used for the split
        double threshold; // split value
        Node left; // records with value <= threshold
        Node right; // records with value > threshold

        // Leaf node
        Node(String leafCategory) {
            this.leafCategory = leafCategory;
        }

        // Split node
        Node(int featureIndex, double threshold) {
            this.featureIndex = featureIndex;
            this.threshold = threshold;
        }
        // A node is a leaf if it has a category
        boolean isLeaf() {
            return leafCategory != null;
        }
    }


    public DecisionTreeClassifier(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public void train(List<UserRecord> trainingData) {
        // Build the tree starting from depth 0
        this.root = buildTree(trainingData, 0);
    }

    @Override
    public String predict(UserRecord user) {
        if (root == null) {
            return "UNKNOWN";
        }
        // Walk down the tree from the root until a leaf is reached
        return traverseTree(user, root);
    }

    // Builds the tree recursively
    private Node buildTree(List<UserRecord> data, int depth) {
        if (data.isEmpty()) {
            return new Node("UNKNOWN");
        }

        // Stop: maximum depth reached
        if (depth >= maxDepth) {
            return new Node(getMajorityCategory(data));
        }
        // Stop: all records have the same category
        if (isPure(data)) {
            return new Node(data.get(0).getCategory());
        }

        // Find the split with the highest information gain
        BestSplit best = findBestSplit(data);

        // No useful split found: make a leaf with the majority category
        if (best == null) {
            return new Node(getMajorityCategory(data));
        }

        // Split the data with the best rule
        List<UserRecord> leftData = new ArrayList<>();
        List<UserRecord> rightData = new ArrayList<>();
        for (UserRecord record : data) {
            if (record.getFeatures()[best.featureIndex] <= best.threshold) {
                leftData.add(record);
            } else {
                rightData.add(record);
            }
        }

        if (leftData.isEmpty() || rightData.isEmpty()) {
            return new Node(getMajorityCategory(data));
        }

        // Create the node and build the left and right subtrees
        // with depth + 1 (recursion)
        Node node = new Node(best.featureIndex, best.threshold);
        node.left = buildTree(leftData, depth + 1);
        node.right = buildTree(rightData, depth + 1);
        return node;
    }


    private record BestSplit(int featureIndex, double threshold, double gain) {
    }

    // Tries every feature and every value as a threshold and returns the best split
    private BestSplit findBestSplit(List<UserRecord> data) {
        double parentEntropy = calculateEntropy(data);
        int featureCount = data.get(0).getFeatures().length;

        BestSplit best = null;
        // Try every feature
        for (int featureIndex = 0; featureIndex < featureCount; featureIndex++) {

            // Every distinct value of the feature is a candidate threshold
            Set<Double> candidateThresholds = new HashSet<>();
            for (UserRecord record : data) {
                candidateThresholds.add(record.getFeatures()[featureIndex]);
            }

            // Try each candidate threshold
            for (double threshold : candidateThresholds) {

                List<UserRecord> leftSubset = new ArrayList<>();
                List<UserRecord> rightSubset = new ArrayList<>();
                // Test split
                for (UserRecord record : data) {
                    if (record.getFeatures()[featureIndex] <= threshold) {
                        leftSubset.add(record);
                    } else {
                        rightSubset.add(record);
                    }
                }


                if (leftSubset.isEmpty() || rightSubset.isEmpty()) {
                    continue;
                }

                // How much the split reduces the entropy (information gain)
                double gain = calculateInformationGain(parentEntropy, data.size(),
                        leftSubset, rightSubset);
                // Keep the split with the highest gain
                if (best == null || gain > best.gain) {
                    best = new BestSplit(featureIndex, threshold, gain);
                }
            }
        }

        if (best != null && best.gain < 1e-10) {
            return null;
        }
        return best;
    }

    // Entropy: how mixed the categories are (0 = all the same)
    public double calculateEntropy(List<UserRecord> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }

        // Count the records of each category
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (UserRecord record : data) {
            String category = record.getCategory();
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        double entropy = 0.0;
        int total = data.size();
        for (int count : categoryCounts.values()) {
            if (count > 0) {
                double probability = (double) count / total;
                // Java has no log2, so log2(p) = log(p) / log(2)
                entropy -= probability * (Math.log(probability) / Math.log(2.0));
            }
        }
        return entropy;
    }

    // Information gain = parent entropy - weighted average entropy of the children
    private double calculateInformationGain(double parentEntropy, int parentSize,
                                            List<UserRecord> left, List<UserRecord> right) {

        // Each child is weighted by its size, otherwise a very small child
        // with entropy 0 would look like a great split
        double weightedChildEntropy =
                ((double) left.size() / parentSize) * calculateEntropy(left)
              + ((double) right.size() / parentSize) * calculateEntropy(right);

        // Bigger gain means a better split
        return parentEntropy - weightedChildEntropy;
    }

    // Walks down the tree and returns the category of the leaf
    private String traverseTree(UserRecord user, Node node) {
        if (node.isLeaf()) {
            return node.leafCategory;
        }
        if (user.getFeatures()[node.featureIndex] <= node.threshold) {
            return traverseTree(user, node.left);
        } else {
            return traverseTree(user, node.right);
        }
    }

    // Returns true if all records have the same category
    private boolean isPure(List<UserRecord> data) {
        if (data.isEmpty()) {
            return true;
        }
        String firstCategory = data.get(0).getCategory();
        for (UserRecord record : data) {
            if (!firstCategory.equals(record.getCategory())) {
                return false;
            }
        }
        return true;
    }

    // Returns the most common category
    private String getMajorityCategory(List<UserRecord> data) {
        Map<String, Integer> counts = new HashMap<>();
        String majority = "UNKNOWN";
        int maxCount = -1;

        for (UserRecord record : data) {
            String category = record.getCategory();
            int updated = counts.getOrDefault(category, 0) + 1;
            counts.put(category, updated);

            // Keep track of the category with the highest count
            if (updated > maxCount) {
                maxCount = updated;
                majority = category;
            }
        }
        return majority;
    }
}
