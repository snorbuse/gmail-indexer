package se.snorbu.gmail.reader;

import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import se.snorbu.gmail.entity.GmailMessage;
import se.snorbu.gmail.service.GmailService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static se.snorbu.gmail.config.ApplicationConfig.MAX_MESSAGES;
import static se.snorbu.gmail.service.GmailService.USER;
import static se.snorbu.gmail.service.GmailService.getMessageInfo;

@Component
@AllArgsConstructor
@Slf4j
public class BatchReader {

    private GmailService gmailService;

    public List<GmailMessage> readMessages() throws IOException {
        List<Message> messagesList = getMessages();

        List<GmailMessage> gmailMessages = new ArrayList<>();
        for (Message msg : messagesList) {
            if (gmailMessages.size() > MAX_MESSAGES) {
                log.info("Breaking collecting");
                break;
            }

            log.info("Extracting info from messageID: {}, {}/{}",  msg.getId(), (gmailMessages.size() +1), MAX_MESSAGES);

            Message message = gmailService.getService()
                    .get(USER, msg.getId())
                    .setFormat("metadata")
                    .execute();

            GmailMessage gmailMessage = GmailMessage.of(
                    message.getId(),
                    getMessageInfo(message, "From"),
                    getMessageInfo(message, "Date"),
                    getMessageInfo(message, "Subject"),
                    ""
            );

            if (gmailMessage != null) {
                gmailMessages.add(gmailMessage);
                log.debug("GmailService message: " + gmailMessage);
            }
        }

        return gmailMessages;
    }

    private List<Message> getMessages() throws IOException {
        ListMessagesResponse response = gmailService.getService()
                .list(USER)
                .execute();

        List<Message> messagesList = new ArrayList<>();
        while (response.getMessages() != null) {
            messagesList.addAll(response.getMessages());

            if (response.getNextPageToken() != null && messagesList.size() < MAX_MESSAGES) {
                String pageToken = response.getNextPageToken();
                response = gmailService.getService()
                        .list(USER)
                        .setPageToken(pageToken)
                        .execute();
            } else {
                break;
            }
        }
        return messagesList;
    }

}
