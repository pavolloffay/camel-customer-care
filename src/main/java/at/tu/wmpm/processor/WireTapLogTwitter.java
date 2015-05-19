package at.tu.wmpm.processor;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

import at.tu.wmpm.exception.TwitterException;

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
		// String subject = (String) exchange.getIn().getHeader("Subject");
		// String from = (String) exchange.getIn().getHeader("From");
		// String to = (String) exchange.getIn().getHeader("To");
		String body = (String) exchange.getIn().getBody();

		body = "Wiretap - TwitterLog on " + timeStamp + "\n" + "> Body:\n"
				+ body;

		exchange.getIn().setBody(body);
	}
}