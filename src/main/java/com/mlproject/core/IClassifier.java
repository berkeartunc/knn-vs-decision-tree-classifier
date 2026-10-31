package com.mlproject.core;

import com.mlproject.data.UserRecord;
import java.util.List;


// Abstraction
// Every classification algorithm in the project has to follow this interface.
// It only says what a classifier does, not how it does it.
public interface IClassifier {

    // Each algorithm implements this in its own way (overriding).
    void train(List<UserRecord> trainingData);

    // Takes a customer record and returns the predicted category
    String predict(UserRecord user);

}
