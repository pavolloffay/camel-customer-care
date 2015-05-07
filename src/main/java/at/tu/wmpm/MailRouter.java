package at.tu.wmpm;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Created by pavol on 30.4.2015.
 */
public class MailRouter extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(MailRouter.class);

    @PostConstruct
    public void postConstruct() {
        log.debug("Mail component initialized");
    }

    @Override
    public void configure() throws Exception {

        from("pop3s://customer.care.tu.wien@pop.gmail.com:995?password=customerCare123").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                log.debug(ReflectionToStringBuilder.toString(exchange));

                Message in = exchange.getIn();
                log.debug("Mail body:\n" + in.getBody(String.class) + "\n" );
                
                //AUTO REPLY
                /*Map<String, Object> test=in.getHeaders();
                for(String t:test.keySet()) {
                	log.debug(t + " " + test.get(t).toString());
                }*/
                //String recipients = "&To=camel@riders.org,easy@riders.org&CC=me@you.org&BCC=someone@somewhere.org";
                
                //from("direct:a").to("smtp://you@mymailserver.com?password=secret&From=you@apache.org" + recipients);
            }
        });
    }
}
