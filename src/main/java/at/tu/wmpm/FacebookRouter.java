package at.tu.wmpm;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pavol on 8.5.2015.
 */
public class FacebookRouter extends RouteBuilder {

    private final Logger log = LoggerFactory.getLogger(FacebookRouter.class);

    private String id = "833818856683698";
    private String secret = "b26922112b372d38d597a2b870397f31";
    private String accessToken = "CAAL2WpTCeLIBAGB8mXZAOasVUjAZBb4UTG1MmZA4bYZASZBOOzPUKBHYrJZBqoHw3yWBt2TO8K5iQJVjsUOeq2hSUSlntciN5ZBROir3AhB9t5HcKOVfwUl39e56RW8nbq1B8ltTSW5AdgGjhH8A8LNWd9USygZBQWPvZCXEREi2HOEDTcJQ2YU5WU89Q2WRoBnImtY8821NPqWtRW05Sqqwe1PpiE6dks38ZD";
    private String accessToken2 = "CAAL2WpTCeLIBAEChiHKSmykJ7QbOcQsXK25O9vze6Ckd7Y23KMMk5ZCJGWNCwTCLkvt93bZBTzNEZBxHvdQakmbZCWWuzsdyNtkoGZCV7frpAjTpcChk8rN17PJiiNpwwc6H5TnAPLrZARJTW0oiZCpPxPTlItWmvoBBZCYmuLFlvUiSH1oGJ4YBR4gFYiKKInKpJ98wCYtZAa2JXqNwqnNSB";

    private String userId = "109370969394044";

    @Override
    public void configure() throws Exception {
        from("facebook://me?oAuthAppId=" + id + "&oAuthAppSecret=" + secret + "&oAuthAccessToken=" + accessToken2 + "&consumer.delay=1000").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {

                log.debug("\n\n\n\n FACEBOOK\n\n\n\n");
                log.debug(ReflectionToStringBuilder.toString(exchange));
            }
        });
    }
}
