package at.tu.wmpm.processor;

import at.tu.wmpm.exception.TwitterException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author Christian
 *
 */
@Service
public class WireTapLogTwitter implements Processor {

    @Override
    public void process(Exchange exchange) throws TwitterException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss")
                .format(Calendar.getInstance().getTime());

        String body = exchange.getIn().getBody(String.class);

        body = "Wiretap - TwitterLog on " + timeStamp + "\n" + "> Body:\n"
                + body;

        exchange.getIn().setBody(body);
    }
}
