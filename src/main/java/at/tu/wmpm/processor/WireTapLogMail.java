package at.tu.wmpm.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

import at.tu.wmpm.exception.MailException;

/**
 * 
 * @author Christian
 *
 */
@Service
public class WireTapLogMail implements Processor {

    @Override
    public void process(Exchange exchange) throws MailException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss")
                .format(Calendar.getInstance().getTime());
        String subject = (String) exchange.getIn().getHeader("Subject");
        String from = (String) exchange.getIn().getHeader("From");
        String to = (String) exchange.getIn().getHeader("To");
        String body = exchange.getIn().getBody(String.class);

        if (subject.equalsIgnoreCase("subject"))
            throw new MailException("Subject mustn't be null!");

        body = "Wiretap - EmailLog on " + timeStamp + "\n" + "> Subject: "
                + subject + "\n" + "> From: " + from + "\n" + "> To: " + to
                + "\n" + "> Body:\n" + body;

        exchange.getIn().setBody(body);
    }
}