package org.redischool.attendance.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnProperty(name = "google.credentials")
    public Sheets sheetsClientFromCredentials(@Value("${google.credentials}") String credentials) {
        try {
            InputStream resourceInputStream = new ByteArrayInputStream(credentials.getBytes());
            GoogleCredential credential =
                    GoogleCredential.fromStream(resourceInputStream).createScoped(SheetsScopes.all());

            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            return new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("Client for sheets-accessor").build();

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    @ConditionalOnMissingBean(Sheets.class)
    @ConditionalOnProperty(name = "google.credentials_path")
    public Sheets sheetsClientFromCredentialsFile(@Value("${google.credentials_path}") String credentialsFile) {
        try {
            InputStream credentialStream = this.getClass().getClassLoader().getResourceAsStream(credentialsFile);
            GoogleCredential credential =
                    GoogleCredential.fromStream(credentialStream).createScoped(SheetsScopes.all());

            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            return new Sheets.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName("Client for sheets-accessor").build();

        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
