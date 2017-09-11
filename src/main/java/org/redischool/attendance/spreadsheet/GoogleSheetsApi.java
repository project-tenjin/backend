package org.redischool.attendance.spreadsheet;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleSheetsApi {
    public static final String RAW_VALUE_INPUT_OPTION = "RAW";

    private String CREDENTIALS_PATH = "./google_sheets_credentials.json";

    public List<Sheet> getSheets(String spreadsheetId) {
        try {
            Sheets.Spreadsheets.Get request = sheetsClient().spreadsheets().get(spreadsheetId);
            return request.execute().getSheets();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<Object>> getRange(String spreadsheetId, String sheetName, String range) {
        try {
            String namedRange = "'" + sheetName + "'!" + range;
            Sheets.Spreadsheets.Values.Get request = sheetsClient().spreadsheets().values().get(spreadsheetId, namedRange);
            return request.execute().getValues();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateDataRange(String spreadsheetId, String courseName, String range, List<List<Object>> dataToWrite) {
        String namedRange = "'" + courseName + "'!" + range;

        ValueRange body = new ValueRange()
                .setValues(dataToWrite);

        try {
            sheetsClient().spreadsheets().values()
                    .update(spreadsheetId, namedRange, body)
                    .setValueInputOption(RAW_VALUE_INPUT_OPTION)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Sheets sheetsClient() {
        try {
            Resource resource = new ClassPathResource(CREDENTIALS_PATH);
            InputStream resourceInputStream = resource.getInputStream();
            GoogleCredential credential = GoogleCredential.fromStream(resourceInputStream)
                    .createScoped(SheetsScopes.all());

            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            return new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("Client for sheets-accessor")
                    .build();

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
