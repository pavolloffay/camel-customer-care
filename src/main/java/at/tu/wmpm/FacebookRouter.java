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
 * Facebook page - Area 51 Customer Care
 *      https://www.facebook.com/area51customercare
 *
 **/
public class FacebookRouter extends RouteBuilder {

    private final Logger log = LoggerFactory.getLogger(FacebookRouter.class);

/*   
 * Not used anymore since using properties file
 * 
 * private String id = "833818856683698";
    private String secret = "b26922112b372d38d597a2b870397f31";
    private String accessToken = "CAAL2WpTCeLIBACzMlCdqMD95lZAqZBVGzECfcnEiZBGbgorFAbsAFuzRwkW8ZAXlnAqssrztm3v7L3U6V280O8pbjP72tMP8zxCc4xEZCZAGMZBUlLL39QO81OSe6ZAobZBvOKFFBg2b5CoGDulZBJLliEJAfNhAScV55wV8yABXW5mZCPPIFKwfMacekHMaUDWvw4ZD";

    private String userId = "109370969394044";
    private String pageId = "1398676863790958";
   from("facebook://getTagged?reading.since=1.1.2015&userId=" + pageId + "&oAuthAppId=" + id + "&oAuthAppSecret=" + secret + "&oAuthAccessToken=" + accessToken).process(new Processor() {
    
    */

    @Override
    public void configure() throws Exception {
        from("facebook://getTagged?reading.since=1.1.2015&userId={{FBpageId}}&oAuthAppId={{FBid}}&oAuthAppSecret={{FBsecret}}&oAuthAccessToken={{FBaccessToken}}").process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {

                log.debug("\n\nFACEBOOK");
                log.debug(ReflectionToStringBuilder.toString(exchange));
                log.debug("\n\n");
            }
        });
    }
}
