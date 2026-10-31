package com.mlproject.algorithms;

import com.mlproject.data.UserRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mlproject.TestRecords.withFeatures;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DecisionTreeClassifierTest {

    private static final double DELTA = 1e-9;

    @Test
    void learnsASimpleThresholdRule() {
        // Rule: feature 1 <= 0.5 -> A, otherwise B (feature 0 is noise)
        List<UserRecord> training = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double value = i / 10.0;
            training.add(withFeatures(value <= 0.5 ? "A" : "B", (i * 7 % 10) / 10.0, value));
        }

        DecisionTreeClassifier tree = new DecisionTreeClassifier(5);
        tree.train(training);

        assertEquals("A", tree.predict(withFeatures("?", 0.9, 0.2)));
        assertEquals("B", tree.predict(withFeatures("?", 0.1, 0.8)));
        assertEquals(100.0, tree.calculateAccuracy(training), DELTA);
    }

    @Test
    void depthZeroAlwaysPredictsTheMajorityCategory() {
        List<UserRecord> training = List.of(
                withFeatures("A", 0.0), withFeatures("A", 0.5), withFeatures("A", 0.7), withFeatures("B", 1.0));

        DecisionTreeClassifier tree = new DecisionTreeClassifier(0);
        tree.train(training);

        assertEquals("A", tree.predict(withFeatures("?", 1.0)));
    }

    @Test
    void entropyOfAPureSetIsZero() {
        DecisionTreeClassifier tree = new DecisionTreeClassifier(5);
        List<UserRecord> pure = List.of(withFeatures("A", 0.0), withFeatures("A", 1.0));

        assertEquals(0.0, tree.calculateEntropy(pure), DELTA);
    }

    @Test
    void entropyOfAnEvenTwoClassSetIsOne() {
        DecisionTreeClassifier tree = new DecisionTreeClassifier(5);
        List<UserRecord> mixed = List.of(
                withFeatures("A", 0.0), withFeatures("A", 0.1), withFeatures("B", 0.9), withFeatures("B", 1.0));

        assertEquals(1.0, tree.calculateEntropy(mixed), DELTA);
    }

    @Test
    void returnsUnknownWhenNotTrained() {
        assertEquals("UNKNOWN", new DecisionTreeClassifier(5).predict(withFeatures("?", 0.5)));
    }
}
