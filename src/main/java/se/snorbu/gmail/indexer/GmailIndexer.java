package se.snorbu.gmail.indexer;

import com.google.api.services.gmail.model.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import se.snorbu.gmail.entity.GmailMessage;
import se.snorbu.gmail.reader.StreamReader;
import se.snorbu.gmail.reactive.MyPublisher;
import se.snorbu.gmail.reactive.MySubscriber;
import se.snorbu.gmail.repository.GmailMessageRespository;
import se.snorbu.gmail.reader.BatchReader;
import se.snorbu.gmail.reader.SerialReader;

import java.io.IOException;
import java.util.List;

import static se.snorbu.gmail.service.GmailService.getMessageInfo;

@Slf4j
@Component
@AllArgsConstructor
public class GmailIndexer implements ApplicationRunner {


    private SerialReader serialReader;
    private BatchReader batchReader;
    private GmailMessageRespository gmailMessageRespository;
    private StreamReader streamReader;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Command runner");

        long startTime = System.nanoTime();
//        streamIndexer();
//        serialIndexer();
        batchIndexer();
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        log.info("Execution time: {} ms", duration);
    }


    private void streamIndexer() {
        MyPublisher myPublisher = new MyPublisher(streamReader);
        myPublisher.subscribe(new MySubscriber(gmailMessageRespository));
    }

    private void serialIndexer() throws IOException {
        while (true) {
            Message message = serialReader.readNextMessage();
            if (message == null) {
                log.info("No more messages, breaking");
                break;
            }

            GmailMessage gmailMessage = GmailMessage.of(
                    message.getId(),
                    getMessageInfo(message, "From"),
                    getMessageInfo(message, "Date"),
                    getMessageInfo(message, "Subject"),
                    String.join(" ", message.getLabelIds())
            );

            if (gmailMessage != null) {
                gmailMessageRespository.save(gmailMessage);
            }
        }
    }

    private void batchIndexer() throws IOException {
        List<GmailMessage> gmailMessages = batchReader.readMessages();
        for (GmailMessage gmailMessage : gmailMessages) {
            log.info("Inserting message: {}", gmailMessage);
            gmailMessageRespository.save(gmailMessage);
        }
    }
}
