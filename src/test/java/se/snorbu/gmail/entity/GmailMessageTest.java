package se.snorbu.gmail.entity;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class GmailMessageTest {

    @Test
    public void shouldCreateEmail() {
        GmailMessage gmailMessage = GmailMessage.of("123", "Kalle Hansson <foo@bar.com>", "Sun, 30 Sep 2018 16:56:24 +0000 (UTC)", "");

        assertThat(gmailMessage.getFrom(), is("Kalle Hansson <foo@bar.com>"));
        assertThat(gmailMessage.getFromAdress(), is("foo@bar.com"));
        assertThat(gmailMessage.getFromDomain(), is("bar.com"));
        assertThat(gmailMessage.getFromLocalPart(), is("foo"));
        assertThat(gmailMessage.getFromUser(), is("Kalle Hansson"));
        assertThat(gmailMessage.getDate(), is("2018-09-30 16:56:24"));
    }

    @Test
    public void shouldCreateDate() {
        GmailMessage gmailMessage = GmailMessage.of("123", "Kalle Hansson <foo@bar.com>", "Sun, 30 Sep 2018 16:56:24 -0500 (UTC)", "");
        assertThat(gmailMessage.getDate(), is("2018-09-30 16:56:24"));
    }


    @Test
    public void shouldCreateDate2() {
        GmailMessage gmailMessage = GmailMessage.of("123", "Kalle Hansson <foo@bar.com>", "30 Sep 2018 16:56:24 -0500 (UTC)", "");
        assertThat(gmailMessage.getDate(), is("2018-09-30 16:56:24"));
    }


    @Test
    public void shouldCreateDate3() {
        GmailMessage gmailMessage = GmailMessage.of("123", "Kalle Hansson <foo@bar.com>", "30 Sep 2018 16:56:24 GMT", "");
        assertThat(gmailMessage.getDate(), is("2018-09-30 16:56:24"));
    }

}