package org.redi_school.attendance;

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
        Sheets sheetsClient = SheetsClient();
        Sheets.Spreadsheets.Get request = null;
        try {
            request = sheetsClient.spreadsheets().get(spreadsheetId);
            return request.execute().getSheets();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<Object>> getRange(String spreadsheetId, String sheetName, String range) {
        Sheets sheetsClient = SheetsClient();
        Sheets.Spreadsheets.Values.Get request = null;
        try {
            String namedRange = "'" + sheetName + "'!" + range;
            request = sheetsClient.spreadsheets().values().get(spreadsheetId, namedRange);
            return request.execute().getValues();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Sheets SheetsClient() {
        GoogleCredential credential = null;
        HttpTransport httpTransport = null;
        try {
            Resource resource = new ClassPathResource(CREDENTIALS_PATH);
            InputStream resourceInputStream = resource.getInputStream();
            credential = GoogleCredential.fromStream(resourceInputStream)
                    .createScoped(SheetsScopes.all());
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        return new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Client for sheets-accessor")
                .build();
    }

    public void updateDataRange(String spreadsheetId, String courseName, String range, List<List<Object>> dataToWrite) {
        String namedRange = "'" + courseName + "'!" + range;

        ValueRange body = new ValueRange()
                .setValues(dataToWrite);

        try {
            SheetsClient().spreadsheets().values()
                    .update(spreadsheetId, namedRange, body)
                    .setValueInputOption(RAW_VALUE_INPUT_OPTION)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
