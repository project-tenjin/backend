package org.redischool.attendance.spreadsheet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;


import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.GridData;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

@Service
public class GoogleSheetsApi {
    public static final String RAW_VALUE_INPUT_OPTION = "RAW";

    private final Sheets sheetsClient;

    public GoogleSheetsApi(Sheets sheetsClient) {

        this.sheetsClient = sheetsClient;
    }

    public List<Sheet> getSheets(String spreadsheetId) {
        try {
            Sheets.Spreadsheets.Get request = sheetsClient.spreadsheets().get(spreadsheetId);
            return request.execute().getSheets();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<Object>> getRange(String spreadsheetId, String sheetName, String range) {
        try {
            String namedRange = "'" + sheetName + "'!" + range;
            Sheets.Spreadsheets.Values.Get request = sheetsClient.spreadsheets().values().get(spreadsheetId, namedRange);
            return request.execute().getValues();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GridData getGridData(String spreadsheetId, String sheetName, String range) {
        try {
            String namedRange = "'" + sheetName + "'!" + range;
            Sheets.Spreadsheets.Get request = sheetsClient.spreadsheets()
                    .get(spreadsheetId)
                    .setRanges(Collections.singletonList(namedRange))
                    .setIncludeGridData(true);
            Spreadsheet sheet = request.execute();
            return sheet.getSheets().get(0).getData().get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDataRange(String spreadsheetId, String courseName, String range, List<List<Object>> dataToWrite) {
        String namedRange = "'" + courseName + "'!" + range;

        ValueRange body = new ValueRange()
                .setValues(dataToWrite);

        try {
            sheetsClient.spreadsheets().values()
                    .update(spreadsheetId, namedRange, body)
                    .setValueInputOption(RAW_VALUE_INPUT_OPTION)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date dateFromSerial(Double serialDate) {
        LocalDateTime referenceDate = LocalDateTime.of(1899, Month.DECEMBER, 30, 0, 0);
        LocalDateTime localDateTime = referenceDate.plusDays(serialDate.longValue());
        Date date = Date.from(localDateTime.atZone(ZoneId.of("Europe/Berlin")).toInstant());
        return date;
    }

}
