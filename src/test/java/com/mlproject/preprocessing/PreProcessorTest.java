package com.mlproject.preprocessing;

import com.mlproject.data.UserRecord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mlproject.TestRecords.raw;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PreProcessorTest {

    private static final double DELTA = 1e-9;

    @Test
    void encodesGenderAsZeroOrOne() {
        List<UserRecord> data = new ArrayList<>(List.of(
                raw("K", 1, 1, 1, "X", "A"), raw("E", 2, 2, 2, "X", "A"),
                raw("KADIN", 3, 3, 3, "X", "A"), raw("ERKEK", 4, 4, 4, "X", "A")));

        new PreProcessor().fitAndTransform(data);

        assertEquals(0.0, data.get(0).getFeatures()[0], DELTA);
        assertEquals(1.0, data.get(1).getFeatures()[0], DELTA);
        assertEquals(0.0, data.get(2).getFeatures()[0], DELTA);
        assertEquals(1.0, data.get(3).getFeatures()[0], DELTA);
    }

    @Test
    void recognizesLowercaseGenderOnATurkishSystem() {
        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            List<UserRecord> data = new ArrayList<>(List.of(
                    raw("kadin", 1, 1, 1, "X", "A"), raw("erkek", 2, 2, 2, "X", "A")));

            new PreProcessor().fitAndTransform(data);

            assertEquals(2, data.size());
            assertEquals(0.0, data.get(0).getFeatures()[0], DELTA);
            assertEquals(1.0, data.get(1).getFeatures()[0], DELTA);
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void removesRecordsWithUnknownGenderOrMissingValues() {
        List<UserRecord> data = new ArrayList<>(List.of(
                raw("K", 1, 1, 1, "X", "A"),
                raw("?", 2, 2, 2, "X", "A"),
                raw("E", 3, 3, 3, " ", "A"),
                raw("E", 4, 4, 4, "X", "")));

        new PreProcessor().fitAndTransform(data);

        assertEquals(1, data.size());
    }

    @Test
    void scalesTrainingValuesBetweenZeroAndOne() {
        List<UserRecord> data = new ArrayList<>(List.of(
                raw("K", 10, 100, 5, "X", "A"), raw("K", 20, 200, 10, "X", "A"), raw("K", 30, 300, 15, "X", "A")));

        new PreProcessor().fitAndTransform(data);

        assertEquals(0.0, data.get(0).getFeatures()[1], DELTA);
        assertEquals(0.5, data.get(1).getFeatures()[1], DELTA);
        assertEquals(1.0, data.get(2).getFeatures()[1], DELTA);
        assertEquals(0.5, data.get(1).getFeatures()[2], DELTA);
        assertEquals(1.0, data.get(2).getFeatures()[3], DELTA);
    }

    @Test
    void testDataIsScaledWithTheTrainingMinAndMax() {
        PreProcessor preProcessor = new PreProcessor();
        preProcessor.fitAndTransform(new ArrayList<>(List.of(
                raw("K", 10, 1, 1, "X", "A"), raw("K", 30, 2, 2, "X", "A"))));

        List<UserRecord> test = new ArrayList<>(List.of(raw("K", 40, 1, 1, "X", "A"), raw("K", 10, 1, 1, "X", "A")));
        preProcessor.transform(test);

        // (40 - 10) / (30 - 10) = 1.5, outside 0-1 because 40 was not in the training data
        assertEquals(1.5, test.get(0).getFeatures()[1], DELTA);
        assertEquals(0.0, test.get(1).getFeatures()[1], DELTA);
    }

    @Test
    void sameBrandAlwaysGetsTheSameId() {
        PreProcessor preProcessor = new PreProcessor();
        List<UserRecord> training = new ArrayList<>(List.of(
                raw("K", 1, 1, 1, "Brand X", "A"), raw("K", 2, 2, 2, "BRAND Y", "A")));
        preProcessor.fitAndTransform(training);

        List<UserRecord> test = new ArrayList<>(List.of(
                raw("K", 1, 1, 1, " brand x ", "A"), raw("K", 1, 1, 1, "BRAND Z", "A")));
        preProcessor.transform(test);

        assertEquals(0.0, training.get(0).getFeatures()[4], DELTA);
        assertEquals(1.0, training.get(1).getFeatures()[4], DELTA);
        assertEquals(0.0, test.get(0).getFeatures()[4], DELTA);
        // A brand that is only in the test data gets the next new id
        assertEquals(2.0, test.get(1).getFeatures()[4], DELTA);
    }
}
