package com.mlproject.evaluation;

import com.mlproject.data.UserRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Evaluator {

    private List<UserRecord> trainData;
    private List<UserRecord> testData;


    public Evaluator() {

        // Empty lists until splitData() is called
        this.trainData = new ArrayList<>();
        this.testData = new ArrayList<>();
    }

    // Shuffles the data and splits it into training and test sets by the given ratio
    public void splitData(List<UserRecord> fullData, double trainRatio) {
        if (fullData == null || fullData.isEmpty()) {
            throw new IllegalArgumentException("Data to split can't be empty.");
        }
        if (trainRatio <= 0.0 || trainRatio >= 1.0) {
            throw new IllegalArgumentException(
                    "trainRatio must be between 0.0 and 1.0 (exclusive).");
        }

        // Work on a copy, so the original list is not changed
        List<UserRecord> shuffled = new ArrayList<>(fullData);

        // Random shuffle, so the split is different on every run
        // and the order of the rows in the Excel file doesn't affect the result
        Collections.shuffle(shuffled);

        // Index where the list is split
        int splitIndex = (int) (shuffled.size() * trainRatio);

        // [0, splitIndex) is the training set
        this.trainData = new ArrayList<>(shuffled.subList(0, splitIndex));

        // [splitIndex, end) is the test set
        this.testData  = new ArrayList<>(shuffled.subList(splitIndex, shuffled.size()));
    }

    // Getters
    public List<UserRecord> getTrainData() {
        return trainData;
    }
    public List<UserRecord> getTestData() {
        return testData;
    }
}
