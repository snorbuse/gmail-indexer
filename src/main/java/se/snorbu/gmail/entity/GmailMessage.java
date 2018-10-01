package se.snorbu.gmail.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Data
@AllArgsConstructor
@Document(indexName = "gmail")
public class GmailMessage {
    @Id
    private String id;
    private String from;
    private String fromAdress;
    private String fromUser;
    private String fromLocalPart;
    private String fromDomain;
    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private String date;
    private String subject;
    private String labels;

    public static GmailMessage of(String id, String from, String date, String subject, String labels) {
        log.debug("Creating new message from: {}, {}, {}, {}", id, from, date, subject);
        if (from == null || from.isEmpty()) {
            return null;
        }

        Pattern emailPattern = Pattern.compile("\\<([^\\>]+)");
        Matcher matcher = emailPattern.matcher(from);

        String fromAdress = from;
        String fromUser = "";
        String fromLocalPart;
        String fromDomain;
        if (matcher.find()) {
            // Monster <jagent@route.monster.com>
            fromAdress = matcher.group(1);
            fromUser = from.replace("<" + fromAdress + ">", "").trim();
            fromLocalPart = fromAdress.split("@")[0];
            fromDomain = fromAdress.split("@")[1];
        } else {
            // info@sedirect.se
            fromLocalPart = fromAdress.split("@")[0];
            fromDomain = fromAdress.split("@")[1];
        }

        String newDate = formatDate(date);

        return new GmailMessage(id, from, fromAdress, fromUser, fromLocalPart, fromDomain, newDate, subject, labels);
    }

    private static String formatDate(String date) {
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-LL-dd HH:mm:ss");
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("d MMM y H:m:s", Locale.ENGLISH);

        Pattern pattern = Pattern.compile("(.+ \\d{2}:\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(date);
        matcher.find();

        String dateString = matcher.group(1).split("[\\+\\-]\\d{4}")[0];
        if (dateString.contains(",")) {
            dateString = dateString.split(",")[1];
        }
        return LocalDateTime.parse(dateString.trim(), inputFormat).format(outputFormat);
    }
}
