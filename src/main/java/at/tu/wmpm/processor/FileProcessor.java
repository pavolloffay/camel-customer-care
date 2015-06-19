package at.tu.wmpm.processor;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Christian
 *
 */
@Service
public class FileProcessor {

    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);

        exchange.getIn().setBody(body);
    }
}
