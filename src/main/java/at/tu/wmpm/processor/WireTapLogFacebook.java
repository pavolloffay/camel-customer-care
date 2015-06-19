package at.tu.wmpm.processor;

import at.tu.wmpm.exception.FacebookException;
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
public class WireTapLogFacebook implements Processor {

    @Override
    public void process(Exchange exchange) throws FacebookException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss")
                .format(Calendar.getInstance().getTime());

        String body = (String) exchange.getIn().getBody();

        body = "Wiretap - EmailLog on " + timeStamp + "\n" + "> Body:\n" + body;

        exchange.getIn().setBody(body);
    }
}
