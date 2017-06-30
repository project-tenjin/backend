package org.redi_school.attendance;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleSheetsApi {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static final String SPREADSHEET_ID = "18vFpXVDYnMvpp0kdgnD8MW7NS-TLWWd6EtSUAi1tLMU";

    public Sheets build() {
        GoogleCredential credential = null;
        try {
            credential = GoogleCredential.fromStream(new FileInputStream("src/main/resources/credentials.json"))
                    .createScoped(SheetsScopes.all());
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Sheets.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("Client for sheets-accessor")
                .build();
    }

    public List<String> getSheetNames() throws IOException {
        Sheets build = build();
        Sheets.Spreadsheets.Get request = build.spreadsheets().get(SPREADSHEET_ID);

        return request.execute().getSheets().stream()
                .map((Sheet sheet) -> sheet.getProperties().getTitle())
                .collect(Collectors.toList());
    }
}
