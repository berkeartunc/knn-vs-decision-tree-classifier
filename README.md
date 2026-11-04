# KNN vs Decision Tree Classifier

![CI](https://github.com/berkeartunc/knn-vs-decision-tree-classifier/actions/workflows/ci.yml/badge.svg)

[Türkçe](README.tr.md)

A Java desktop app that predicts which product category a customer buys from, using real retail sales data. It compares two classifiers that I wrote from scratch, without any machine learning library: **K-Nearest Neighbors (KNN)** and a **Decision Tree**.

## What it does

- **Loads data from Excel** (Apache POI). Columns are matched by header name, so their order doesn't matter. Comma decimals (`12,5`) and invalid rows are handled.
- **Preprocessing:** gender is encoded as 0/1, brands are label encoded, and amount, price and line total are scaled to 0-1 with min-max normalization. The min-max values are learned from the training data only.
- **KNN:** Euclidean distance and majority vote of the k nearest neighbors.
- **Decision Tree:** splits are chosen by information gain (entropy), with a maximum depth limit.
- **Evaluation:** random train/test split, accuracy and confusion matrix.
- **Swing UI:** one file with a train ratio, or separate train and test files. Run history, accuracy and runtime charts (JFreeChart). The UI language is Turkish because the dataset is Turkish.

## Results

Dataset: 4,625 sales records, 11 product categories, 266 brands. 80% training, 20% test. Every number is the average of 20 runs with a different random split each time (all models use the same 20 splits).

| Model | Accuracy (average) | Range | Time (train + test) |
|---|---|---|---|
| KNN, k = 3 | **89.4%** | 87.2 - 91.1% | ~0.2 s |
| Decision Tree, max depth = 5 | 63.1% | 58.6 - 69.3% | ~0.7 s |
| Decision Tree, max depth = 10 | 80.9% | 77.1 - 84.6% | ~0.8 s |
| Decision Tree, max depth = 15 | **87.8%** | 84.0 - 90.8% | ~0.8 s |

For comparison, always predicting the most common category gives 36.3%.

## Findings: why does KNN look better?

At first KNN looked much better than the Decision Tree (89% vs 63%). I ran a few extra experiments to understand why:

| Experiment | Accuracy |
|---|---|
| No model: predict the most common category of the customer's **brand** | 88.8% |
| KNN, brand id scaled to 0-1 like the other features | 76.0% |
| KNN, brand not used at all | 53.6% |

- **The brand decides most of the result.** 242 of the 266 brands are sold in only one category, so the brand alone gives 88.8%.
- **The brand id is not scaled.** It goes from 0 to 265, while the other features are between 0 and 1. That's why it dominates the Euclidean distance, and KNN mostly finds records of the same brand. When the brand id is scaled like the other features, KNN drops to 76%.
- **Depth 5 is too shallow for the tree.** With 266 brand ids, the tree needs more splits. At depth 15 it reaches 87.8%, close to KNN.

So the difference is mostly about feature encoding and the depth setting, not about one algorithm being better in general.

**Possible next steps:** one-hot encoding for brands, k-fold cross-validation, and tuning k and the maximum depth.

## Project structure

```
src/main/java/com/mlproject/
├── Main.java                     entry point
├── core/                         IClassifier (interface), BaseAlgorithm (accuracy, confusion matrix)
├── algorithms/                   KNNClassifier, DecisionTreeClassifier
├── data/                         DataLoader (Excel), UserRecord
├── preprocessing/PreProcessor    encoding and min-max normalization
├── evaluation/Evaluator          train/test split
└── gui/MainFrame                 Swing UI
src/test/java/com/mlproject/      unit tests
examples/sample-sales.xlsx        small made-up dataset to try the app
```

Both algorithms implement `IClassifier` and extend `BaseAlgorithm`, so the accuracy and confusion matrix code is shared. A new algorithm only needs `train()` and `predict()`.

## Getting started

Requirements: JDK 21 or newer and Maven.

1. Open the project in IntelliJ IDEA. It will import the dependencies from `pom.xml`.
2. Run `Main.java`.
3. Select an Excel file, choose an algorithm and its parameter, and click **Modeli Çalıştır** (Run model).

The Excel file needs these columns: `CLIENTCODE`, `GENDER`, `AMOUNT`, `PRICE`, `LINENETTOTAL`, `BRAND`, `CATEGORY_NAME1`. The dataset used for the results above is real sales data, so it is not included in this repository. To try the app, use `examples/sample-sales.xlsx`: 600 made-up rows with the same columns (fictional brands and customers). The accuracy on this file is not comparable to the table above.

## Tests

```
mvn test
```

27 unit tests (JUnit 5) cover KNN voting, the decision tree (entropy, splits, depth limit), accuracy and the confusion matrix, preprocessing (gender encoding, min-max scaling, brand ids), the train/test split, reading Excel files, and correct behavior on a Turkish-locale system.

## Tech stack

Java 21, Maven, Apache POI, JFreeChart, Swing, JUnit 5
