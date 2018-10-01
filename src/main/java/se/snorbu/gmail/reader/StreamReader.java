package se.snorbu.gmail.reader;

import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import se.snorbu.gmail.service.GmailService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static se.snorbu.gmail.config.ApplicationConfig.MAX_MESSAGES;
import static se.snorbu.gmail.service.GmailService.USER;

@Component
@RequiredArgsConstructor
public class StreamReader implements Iterator<Message> {

    private int current = 0;
    private ListMessagesResponse response;
    private List<Message> messages = new ArrayList<>();

    @NonNull
    private GmailService gmailService;

    @Override
    public boolean hasNext() {
        return current < MAX_MESSAGES;
    }

    @Override
    public Message next() {
        if (messages.isEmpty()) {
            if (response == null || response.getNextPageToken() != null) {
                String pageToken = response == null ? null : response.getNextPageToken();
                try {
                    response = gmailService.getService()
                            .list(USER)
                            .setPageToken(pageToken)
                            .execute();
                    fetchMessages();
                } catch (IOException e) {
                    // TODO don't do this! Handle the error
                    e.printStackTrace();
                }
            }
        }

        Message message = messages.remove(0);

        Message mail = null;
        try {
            mail = gmailService.getService()
                    .get(USER, message.getId())
                    .setFormat("metadata")
                    .execute();
        } catch (IOException e) {
            // TODO don't do this! Handle the error
            e.printStackTrace();
        }

        current++;
        return mail;
    }

    private void fetchMessages() {
        List<Message> newMessages = response.getMessages();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
    }

}
