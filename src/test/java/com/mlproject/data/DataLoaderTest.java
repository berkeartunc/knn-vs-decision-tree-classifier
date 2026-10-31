package com.mlproject.data;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataLoaderTest {

    private static final double DELTA = 1e-9;

    // Columns are in a different order than the default one on purpose
    private static final String[] HEADER =
            {"BRAND", "CLIENTCODE", "CATEGORY_NAME1", "GENDER", "PRICE", "AMOUNT", "LINENETTOTAL"};

    private static Path createExcel(String[] header, Object[][] rows) throws Exception {
        Path file = Files.createTempFile("sales-test", ".xlsx");
        file.toFile().deleteOnExit();
        try (Workbook workbook = new XSSFWorkbook(); OutputStream out = Files.newOutputStream(file)) {
            Sheet sheet = workbook.createSheet();
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < header.length; i++) {
                headerRow.createCell(i).setCellValue(header[i]);
            }
            for (int r = 0; r < rows.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < rows[r].length; c++) {
                    Object value = rows[r][c];
                    if (value instanceof Number number) {
                        row.createCell(c).setCellValue(number.doubleValue());
                    } else if (value != null) {
                        row.createCell(c).setCellValue((String) value);
                    }
                }
            }
            workbook.write(out);
        }
        return file;
    }

    @Test
    void readsColumnsByHeaderNameAndSkipsInvalidRows() throws Exception {
        Path file = createExcel(HEADER, new Object[][]{
                {"ULKER", "C1", "GIDA", "K", 4.5, 2, 9},
                {"PINAR", "C2", "SUT", "E", "12,5", "1", "12,5"},   // comma decimals as text
                {null, "C3", "GIDA", "K", 1, 1, 1},                 // missing brand
                {"ETI", "C4", "GIDA", "E", "abc", 1, 1},            // text in a number column
        });

        List<UserRecord> records = new DataLoader().loadData(file.toString());

        assertEquals(2, records.size());
        UserRecord first = records.get(0);
        assertEquals("C1", first.getClientCode());
        assertEquals("ULKER", first.getBrand());
        assertEquals("GIDA", first.getCategory());
        assertEquals(2.0, first.getAmount(), DELTA);
        assertEquals(4.5, first.getPrice(), DELTA);
        assertEquals(9.0, first.getLineNetTotal(), DELTA);
        assertEquals(12.5, records.get(1).getPrice(), DELTA);
    }

    @Test
    void readsLowercaseHeadersOnATurkishSystem() throws Exception {
        String[] lowercase = new String[HEADER.length];
        for (int i = 0; i < HEADER.length; i++) {
            lowercase[i] = HEADER[i].toLowerCase(Locale.ROOT);
        }
        Path file = createExcel(lowercase, new Object[][]{{"ULKER", "C1", "GIDA", "K", 4.5, 2, 9}});

        Locale original = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            List<UserRecord> records = new DataLoader().loadData(file.toString());

            assertEquals(1, records.size());
            assertEquals("C1", records.get(0).getClientCode());
            assertEquals(4.5, records.get(0).getPrice(), DELTA);
            assertEquals(9.0, records.get(0).getLineNetTotal(), DELTA);
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void returnsAnEmptyListWhenTheFileDoesNotExist() {
        List<UserRecord> records = new DataLoader().loadData("does-not-exist.xlsx");

        assertTrue(records.isEmpty());
    }
}
