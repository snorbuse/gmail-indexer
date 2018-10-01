package se.snorbu.gmail.repository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GmailQuickstart {
    private static final String APPLICATION_NAME = "GmailService API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    public static final String USER = "me";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GmailQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        ListMessagesResponse response = service.users().messages().list(USER).execute();
        log.info("Message count: " + response.getResultSizeEstimate());

        int maxIterations = 5;
        int currentIteration = 0;
        List<Message> messagesList = new ArrayList<>();
        while (response.getMessages() != null) {
            messagesList.addAll(response.getMessages());
            if (response.getNextPageToken() != null && currentIteration < maxIterations) {
                String pageToken = response.getNextPageToken();
                response = service.users().messages().list(USER).setPageToken(pageToken).execute();
            } else {
                break;
            }

            currentIteration++;
        }

//        log.info("Message: " + message.toPrettyString());

        for (Message msg : messagesList) {
            log.info("Extracting info from messageID: " + msg.getId());
            Message message = service.users().messages()
                    .get(USER, msg.getId())
                    .setFormat("metadata")
                    .execute();

            GmailMessage gmailMessage = new GmailMessage(
                    getMessageInfo(message, "From"),
                    getMessageInfo(message, "Date"),
                    getMessageInfo(message, "Subject")
            );

            log.info("GmailService msg: " + gmailMessage);
        }
    }

    private static String getMessageInfo(Message message, String key) {
        Optional<String> first = message.getPayload().getHeaders().stream()
                .filter(o -> o.getName().equals(key))
                .map(MessagePartHeader::getValue)
                .findFirst();
        return first.orElse("");
    }

    @Data
    @AllArgsConstructor
    private static class GmailMessage {
        private String from;
        private String date;
        private String subject;
    }

}