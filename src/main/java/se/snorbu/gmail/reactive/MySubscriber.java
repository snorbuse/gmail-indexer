package se.snorbu.gmail.reactive;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import se.snorbu.gmail.entity.GmailMessage;
import se.snorbu.gmail.repository.GmailMessageRespository;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class MySubscriber implements Subscriber<Message> {

    private GmailMessageRespository gmailMessageRespository;

    @Override
    public void onSubscribe(Subscription subscription) {
    }

    @Override
    public void onNext(Message message) {
        log.info("This messageId is: {}", message.getId());

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

    @Override
    public void onError(Throwable throwable) {
        log.error("Error: " + throwable.getMessage(), throwable);
    }

    @Override
    public void onComplete() {
        log.info("Completed");
    }


    private String getMessageInfo(Message message, String key) {
        Optional<String> first = message.getPayload().getHeaders().stream()
                .filter(o -> o.getName().equals(key))
                .map(MessagePartHeader::getValue)
                .findFirst();
        return first.orElse("");
    }
}
