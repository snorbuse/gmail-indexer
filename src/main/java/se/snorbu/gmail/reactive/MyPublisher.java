package se.snorbu.gmail.reactive;

import com.google.api.services.gmail.model.Message;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import se.snorbu.gmail.reader.StreamReader;

public class MyPublisher implements Publisher<Message> {
    private final StreamReader streamReader;

    public MyPublisher(StreamReader streamReader) {
        this.streamReader = streamReader;
    }

    @Override
    public void subscribe(Subscriber<? super Message> subscriber) {
        try {
            while (streamReader.hasNext()) {
                subscriber.onNext(streamReader.next());
            }
            subscriber.onComplete();
        } catch (Throwable e) {
            subscriber.onError(e);
        }
    }
}
