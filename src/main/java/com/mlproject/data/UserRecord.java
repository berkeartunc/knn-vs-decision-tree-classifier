package com.mlproject.data;


// Data class that holds one row of the Excel file (one sale record).
// Each valid row becomes one UserRecord object.
public class UserRecord {

    // Fields are private, so they can't be changed
    // from outside the class.
    // Raw values from Excel

    private String clientCode;
    private String gender;
    private double amount;
    private double price;
    private double lineNetTotal;
    private String brand;
    private String category; // target value (what is predicted)

    // Feature vector used by the algorithms, filled by PreProcessor:
    // [0] gender, [1] amount, [2] price, [3] line net total (scaled to 0-1),
    // [4] brand id (label encoded)
    private double[] features;

    // DataLoader calls this constructor for every valid row.

    public UserRecord(String clientCode, String gender, double amount, double price,
                      double lineNetTotal, String brand, String category, double[] features) {
        this.clientCode   = clientCode;
        this.gender       = gender;
        this.amount       = amount;
        this.price        = price;
        this.lineNetTotal = lineNetTotal;
        this.brand        = brand;
        this.category     = category;
        this.features     = features;
    }

    // Only getters: values read from Excel should not change later,
    // so there are no setters.
    public String getClientCode() { return clientCode; }

    public String getGender() { return gender; }

    public double getAmount() { return amount; }

    public double getPrice() { return price; }

    public double getLineNetTotal() { return lineNetTotal; }

    public String getBrand() { return brand; }

    public String getCategory() { return category; }

    public double[] getFeatures() { return features; }
}
