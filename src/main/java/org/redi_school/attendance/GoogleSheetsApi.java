package org.redi_school.attendance;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsApi {
    private String SPREADSHEET_ID = "18vFpXVDYnMvpp0kdgnD8MW7NS-TLWWd6EtSUAi1tLMU";
    private String CREDENTIALS_PATH = "./google_sheets_credentials.json";

    public List<String> getSheetNames() {
        Sheets sheetsClient = SheetsClient();
        Sheets.Spreadsheets.Get request = null;
        try {
            request = sheetsClient.spreadsheets().get(SPREADSHEET_ID);
            return request.execute().getSheets().stream()
                    .map((Sheet sheet) -> sheet.getProperties().getTitle())
                    .collect(Collectors.toList());
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
}
