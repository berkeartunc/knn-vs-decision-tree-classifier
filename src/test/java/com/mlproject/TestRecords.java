package com.mlproject;

import com.mlproject.data.UserRecord;

// Helper methods to create small records for the tests
public final class TestRecords {

    private TestRecords() {
    }

    // Raw record, like the ones DataLoader creates (features are filled later)
    public static UserRecord raw(String gender, double amount, double price, double total,
                                 String brand, String category) {
        return new UserRecord("C1", gender, amount, price, total, brand, category, new double[5]);
    }

    // Record with ready feature values, for testing the algorithms directly
    public static UserRecord withFeatures(String category, double... features) {
        return new UserRecord("C1", "E", 0, 0, 0, "BRAND", category, features);
    }
}
