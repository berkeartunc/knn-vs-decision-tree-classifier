package com.mlproject.algorithms;

import com.mlproject.data.UserRecord;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.mlproject.TestRecords.withFeatures;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KNNClassifierTest {

    @Test
    void predictsTheCategoryOfTheClosestGroup() {
        List<UserRecord> training = List.of(
                withFeatures("A", 0.0, 0.0), withFeatures("A", 0.1, 0.0), withFeatures("A", 0.0, 0.1),
                withFeatures("B", 1.0, 1.0), withFeatures("B", 0.9, 1.0), withFeatures("B", 1.0, 0.9));

        KNNClassifier knn = new KNNClassifier(3);
        knn.train(training);

        assertEquals("A", knn.predict(withFeatures("?", 0.05, 0.05)));
        assertEquals("B", knn.predict(withFeatures("?", 0.95, 0.95)));
    }

    @Test
    void usesMajorityVoteOfTheKNearestNeighbors() {
        // Distances from 0: A = 1, B = 2, B = 3
        List<UserRecord> training = List.of(
                withFeatures("A", 1.0), withFeatures("B", 2.0), withFeatures("B", 3.0));

        KNNClassifier oneNeighbor = new KNNClassifier(1);
        oneNeighbor.train(training);
        KNNClassifier threeNeighbors = new KNNClassifier(3);
        threeNeighbors.train(training);

        assertEquals("A", oneNeighbor.predict(withFeatures("?", 0.0)));
        assertEquals("B", threeNeighbors.predict(withFeatures("?", 0.0)));
    }

    @Test
    void worksWhenKIsLargerThanTheTrainingSet() {
        KNNClassifier knn = new KNNClassifier(10);
        knn.train(List.of(withFeatures("A", 0.0), withFeatures("A", 1.0)));

        assertEquals("A", knn.predict(withFeatures("?", 0.5)));
    }

    @Test
    void returnsUnknownWhenNotTrained() {
        assertEquals("UNKNOWN", new KNNClassifier(3).predict(withFeatures("?", 0.5)));
    }
}
