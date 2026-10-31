package com.mlproject.core;

import com.mlproject.data.UserRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.mlproject.TestRecords.withFeatures;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseAlgorithmTest {

    private static final double DELTA = 1e-9;

    // A fake classifier that always predicts "A", so the expected results are easy to calculate
    private static BaseAlgorithm alwaysA() {
        return new BaseAlgorithm() {
            @Override
            public void train(List<UserRecord> trainingData) {
            }

            @Override
            public String predict(UserRecord user) {
                return "A";
            }
        };
    }

    @Test
    void calculatesAccuracyAsAPercentage() {
        List<UserRecord> test = List.of(
                withFeatures("A"), withFeatures("A"), withFeatures("B"), withFeatures("A"));

        assertEquals(75.0, alwaysA().calculateAccuracy(test), DELTA);
    }

    @Test
    void fillsTheConfusionMatrix() {
        BaseAlgorithm classifier = alwaysA();
        classifier.calculateAccuracy(List.of(
                withFeatures("A"), withFeatures("A"), withFeatures("B"), withFeatures("A")));

        Map<String, Map<String, Integer>> matrix = classifier.getRawConfusionMatrix();
        assertEquals(3, matrix.get("A").get("A"));
        assertEquals(1, matrix.get("B").get("A"));
    }

    @Test
    void accuracyOfEmptyTestDataIsZero() {
        assertEquals(0.0, alwaysA().calculateAccuracy(List.of()), DELTA);
    }

    @Test
    void reportSaysWhenTheMatrixIsNotCreated() {
        assertEquals("Confusion matrix is not created yet.", alwaysA().getConfusionMatrixReport());
    }
}
