package at.tu.wmpm;

import at.tu.wmpm.processor.FacebookProcessor;
import at.tu.wmpm.processor.MailProcessor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import javax.annotation.PostConstruct;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 07.05.2015
 */
public class RouteConfig extends RouteBuilder {

private static final Logger log = LoggerFactory.getLogger(RouteConfig.class);

    @Autowired
    private MailProcessor mailProcessor;
    @Autowired
    private FacebookProcessor facebookProcessor;

    @PostConstruct
    public void postConstruct() {
        log.debug("Configuring routes");
    }

    @Override
    public void configure() throws Exception {
        from("pop3s://{{eMailUserName}}@{{eMailPOPAddress}}:{{eMailPOPPort}}?password={{eMailPassword}}")
            .process(mailProcessor)
                .to("velocity:mail-templates/auto-reply.vm")
                .to("smtps://{{eMailSMTPAddress}}:{{eMailSMTPPort}}?password={{eMailPassword}}&username={{eMailUserName}}");


        from("facebook://getTagged?reading.since=1.1.2015&userId={{FBpageId}}&oAuthAppId={{FBid}}&oAuthAppSecret={{FBsecret}}&oAuthAccessToken={{FBaccessToken}}")
            .process(facebookProcessor);
    }
}
