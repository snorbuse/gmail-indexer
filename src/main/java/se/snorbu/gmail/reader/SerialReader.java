package se.snorbu.gmail.reader;

import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.snorbu.gmail.service.GmailService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static se.snorbu.gmail.config.ApplicationConfig.MAX_MESSAGES;
import static se.snorbu.gmail.service.GmailService.USER;

@Slf4j
@Component
@RequiredArgsConstructor
public class SerialReader {

    @NonNull
    private GmailService gmailService;

    private ListMessagesResponse response;
    private List<Message> messages = new ArrayList<>();
    private int messagesFetched = 0;

    private void fetchMessages() {
        List<Message> newMessages = response.getMessages();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
    }

    public Message readNextMessage() throws IOException {
        if (messagesFetched == MAX_MESSAGES) {
            return null;
        }

        if (messages.isEmpty()) {
            if (response == null || response.getNextPageToken() != null) {
                String pageToken = response == null ? null : response.getNextPageToken();
                response = gmailService.getService()
                        .list(USER)
                        .setPageToken(pageToken)
                        .execute();
                fetchMessages();
            }
        }

        Message message = messages.remove(0);
        log.info("Extracting info from messageID: {}, {}/{}", message.getId(), (messagesFetched + 1), MAX_MESSAGES);

        Message mail = gmailService.getService()
                .get(USER, message.getId())
                .setFormat("metadata")
                .execute();

        messagesFetched++;
        return mail;
    }
}
