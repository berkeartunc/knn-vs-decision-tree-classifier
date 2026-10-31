package com.mlproject.data;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DataLoader {

    public List<UserRecord> loadData(String filePath) {

        List<UserRecord> records = new ArrayList<>();

        // try-with-resources: the file and the workbook are closed automatically,
        // even if an error happens (no resource leak).

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Column indexes are found from the header row, so the column order
            // in the Excel file doesn't matter. -1 means not found yet.
            int codeIdx = -1, genderIdx = -1, amountIdx = -1, priceIdx = -1,
                totalIdx = -1, brandIdx = -1, catIdx = -1;

            // Read the header row and match the column names
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                DataFormatter fmt = new DataFormatter();
                for (Cell c : headerRow) {
                    String h = fmt.formatCellValue(c).trim().toUpperCase(Locale.ROOT);
                    switch (h) {
                        case "CLIENTCODE":    codeIdx   = c.getColumnIndex(); break;
                        case "GENDER":        genderIdx = c.getColumnIndex(); break;
                        case "AMOUNT":        amountIdx = c.getColumnIndex(); break;
                        case "PRICE":         priceIdx  = c.getColumnIndex(); break;
                        case "LINENETTOTAL":  totalIdx  = c.getColumnIndex(); break;
                        case "BRAND":         brandIdx  = c.getColumnIndex(); break;
                        case "CATEGORY_NAME1": catIdx   = c.getColumnIndex(); break;
                    }
                }
            }

            // If a header is missing, fall back to the default column order
            if (codeIdx == -1)   codeIdx   = 0;
            if (genderIdx == -1) genderIdx = 1;
            if (amountIdx == -1) amountIdx = 2;
            if (priceIdx == -1)  priceIdx  = 3;
            if (totalIdx == -1)  totalIdx  = 4;
            if (brandIdx == -1)  brandIdx  = 5;
            if (catIdx == -1)    catIdx    = 6;

            // DataFormatter reads any cell type as the text shown in Excel
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // skip the header row

                try {
                    String clientCode = getCellStr(formatter, row, codeIdx);
                    String gender     = getCellStr(formatter, row, genderIdx);

                    // Turkish Excel files use a comma as the decimal separator,
                    // so it is replaced with a dot before parsing
                    String amountStr  = getCellStr(formatter, row, amountIdx).replace(",", ".");
                    String priceStr   = getCellStr(formatter, row, priceIdx).replace(",", ".");
                    String totalStr   = getCellStr(formatter, row, totalIdx).replace(",", ".");
                    String brand      = getCellStr(formatter, row, brandIdx);
                    String category   = getCellStr(formatter, row, catIdx);

                    // Skip the row if any required cell is empty
                    if (clientCode.isEmpty() || gender.isEmpty() || brand.isEmpty()
                            || category.isEmpty() || amountStr.isEmpty()
                            || priceStr.isEmpty() || totalStr.isEmpty()) {
                        continue;
                    }


                    double amount, price, lineNetTotal;
                    try {
                        // Convert the text values to numbers
                        amount       = Double.parseDouble(amountStr);
                        price        = Double.parseDouble(priceStr);
                        lineNetTotal = Double.parseDouble(totalStr);
                    } catch (NumberFormatException e) {
                        // Skip the row if a number cell contains text
                        continue;
                    }
                    // The feature array is filled later by PreProcessor
                    records.add(new UserRecord(clientCode, gender, amount, price, lineNetTotal, brand, category, new double[5]));

                } catch (Exception e) {
                    System.out.println("Row skipped (error): " + row.getRowNum() + " -> " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Could not read the file: " + e.getMessage());
        } finally {
            System.out.println("File reading finished.");
        }

        return records;
    }

    // Reads a cell safely, returns "" if the cell is missing
    private String getCellStr(DataFormatter fmt, Row row, int idx) {
        if (idx == -1) return "";
        Cell cell = row.getCell(idx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return fmt.formatCellValue(cell).trim();
    }
}
