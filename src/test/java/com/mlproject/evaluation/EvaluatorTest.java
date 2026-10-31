package com.mlproject.evaluation;

import com.mlproject.data.UserRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import static com.mlproject.TestRecords.withFeatures;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluatorTest {

    private static List<UserRecord> records(int count) {
        List<UserRecord> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(withFeatures("A", i));
        }
        return list;
    }

    @Test
    void splitsByTheGivenRatio() {
        Evaluator evaluator = new Evaluator();
        evaluator.splitData(records(100), 0.8);

        assertEquals(80, evaluator.getTrainData().size());
        assertEquals(20, evaluator.getTestData().size());
    }

    @Test
    void trainingAndTestSetsDoNotOverlap() {
        List<UserRecord> data = records(50);
        Evaluator evaluator = new Evaluator();
        evaluator.splitData(data, 0.7);

        Set<UserRecord> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        seen.addAll(evaluator.getTrainData());
        for (UserRecord record : evaluator.getTestData()) {
            assertTrue(seen.add(record), "a record is in both sets");
        }
        assertEquals(data.size(), seen.size());
    }

    @Test
    void doesNotChangeTheOriginalList() {
        List<UserRecord> data = records(20);
        List<UserRecord> copy = new ArrayList<>(data);

        new Evaluator().splitData(data, 0.5);

        assertEquals(copy, data);
    }

    @Test
    void rejectsInvalidRatios() {
        Evaluator evaluator = new Evaluator();
        List<UserRecord> data = records(10);

        assertThrows(IllegalArgumentException.class, () -> evaluator.splitData(data, 0.0));
        assertThrows(IllegalArgumentException.class, () -> evaluator.splitData(data, 1.0));
        assertThrows(IllegalArgumentException.class, () -> evaluator.splitData(data, -0.5));
    }

    @Test
    void rejectsEmptyData() {
        Evaluator evaluator = new Evaluator();

        assertThrows(IllegalArgumentException.class, () -> evaluator.splitData(new ArrayList<>(), 0.8));
        assertThrows(IllegalArgumentException.class, () -> evaluator.splitData(null, 0.8));
    }
}
