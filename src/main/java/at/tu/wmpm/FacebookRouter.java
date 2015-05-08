package at.tu.wmpm;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by pavol on 8.5.2015.
 *
 * Access token is probably valid only for short period of time
 *      get accessToken - https://developers.facebook.com/tools/explorer/145634995501895/
 *
 * Facebook app - Customer Care
 *      https://developers.facebook.com/apps/833818856683698/dashboard/
 *
 **/
public class FacebookRouter extends RouteBuilder {

    private final Logger log = LoggerFactory.getLogger(FacebookRouter.class);

    private String id = "833818856683698";
    private String secret = "b26922112b372d38d597a2b870397f31";
    private String accessToken = "CAACEdEose0cBANE8tV31jEJvZC8W0CUbXNNxlhG56Kv5IvpN3EwqPvcyULtZAfAwZCZBJAbQCgVkPsYqlWTmZCddWWWvgZAudVZA2wbhoncwBPPikLdZCkBwXMpnhI7XHr8hiJvu58Y6BGbSSBmKTZAmR5GJyIeqQrFQIOKU9z6eH6gVEgXR33EFsDaEmXXhNDfLDEXgkxVm7cEIhSLmvwPM8";

    private String userId = "109370969394044";

    @Override
    public void configure() throws Exception {
        from("facebook://getPosts?reading.since=1.1.2015&oAuthAppId=" + id + "&oAuthAppSecret=" + secret + "&oAuthAccessToken=" + accessToken).process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {

                log.debug("\n\nFACEBOOK");
                log.debug(ReflectionToStringBuilder.toString(exchange));
                log.debug("\n\n");
            }
        });
    }
}
