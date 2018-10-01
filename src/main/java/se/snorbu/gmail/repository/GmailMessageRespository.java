package se.snorbu.gmail.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import se.snorbu.gmail.entity.GmailMessage;

public interface GmailMessageRespository extends ElasticsearchRepository<GmailMessage, String> {
}
