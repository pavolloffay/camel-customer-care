package at.tu.wmpm.route;

import org.apache.camel.LoggingLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.tu.wmpm.processor.EmployeeFacebookSimulationProcessor;
import at.tu.wmpm.processor.EmployeeMailSimulationProcessor;
import at.tu.wmpm.processor.EmployeeTwitterSimulationProcessor;

/**
 * Simulation for the employee
 *
 * @author Christian
 *
 */
@Component
public class EmployeeSimulationRoute extends CustomRouteBuilder {

    @Autowired
    private EmployeeFacebookSimulationProcessor employeeFacebookSimulationProcessor;
    @Autowired
    private EmployeeMailSimulationProcessor employeeMailSimulationProcessor;
    @Autowired
    private EmployeeTwitterSimulationProcessor employeeTwitterSimulationProcessor;


    @Override
    @SuppressWarnings("deprecation")
    public void configure() throws Exception {
        super.configure();



        /**
         * An employee answers to a TwitterBusinessCase
         * every 45 seconds
         */
        from("quartz2://employeeGroup/commentTwitterTimer?cron=0/45+*+*+*+*+?")
                .setBody()
                .constant("{ \"status\": \"OPEN\" }")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.twitterBcCollection}}&operation=findAll")
                .bean(employeeTwitterSimulationProcessor,
                        "commentOnTwitterBusinessCase")
                // .setBody(header("CamelTwitter.message"))
               // .setHeader("CamelTwitter.message", header("CamelTwitter.message"))
                .to("twitter://timeline/user?consumerKey={{twitter.consumer.key}}&"
                        + "consumerSecret={{twitter.consumer.secret}}&accessToken={{twitter.access.token}}&"
                        + "accessTokenSecret={{twitter.access.token.secret}}")
                .log(LoggingLevel.INFO, org.slf4j.LoggerFactory.getLogger("CustomRouteBuilder.class"), "Twitter-request was answered")
                .end();

        /**
         * An employee answers (aka adds comment) to a FacebookBusinessCase
         * every 45 seconds
         */
        from("quartz2://employeeGroup/commentFacebookTimer?cron=0/45+*+*+*+*+?")
                .setBody()
                .constant("{ \"status\": \"OPEN\" }")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.facebookBcCollection}}&operation=findAll")
                .bean(employeeFacebookSimulationProcessor,
                        "commentOnFacebookBusinessCase")
                .to("facebook://commentPost?postId="
                        + header("CamelFacebook.postId") + "&" + "message="
                        + header("CamelFacebook.message"))
                .log(LoggingLevel.INFO, org.slf4j.LoggerFactory.getLogger("CustomRouteBuilder.class"), "Facebook-request was answered")
                .end();

        /**
         * An employee closes a Facebook ticket (aka Facebook post or open
         * FacebookBusinessCase), every 55 seconds (includes leaving a comment)
         */
        from("quartz2://employeeGroup/closeFacebookTimer?cron=0/15+*+*+*+*+?")
                .setBody()
                .constant("{ \"status\": \"OPEN\" }")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.facebookBcCollection}}&operation=findAll")
                .bean(employeeFacebookSimulationProcessor,
                        "closeFacebookBusinessCase")
                .wireTap("direct:updateBusinessCaseMongo")
                .to("facebook://commentPost?postId="
                        + header("CamelFacebook.postId") + "&" + "message="
                        + header("CamelFacebook.message"))
                 .log(LoggingLevel.INFO, org.slf4j.LoggerFactory.getLogger("CustomRouteBuilder.class"), "Facebook-request was answered and closed")
                .end();

        /**
         * Updates the FacebookBusinessCase in the mongoDB
         */
        from("direct:updateBusinessCaseMongo")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.facebookBcCollection}}&operation=save")
                .end();

        /**
         * An employee closes a Twitter ticket (open
         * TwitterBusinessCase), every 55 seconds (includes leaving a comment)
         */
        from("quartz2://employeeGroup/closeTwitterTimer?cron=0/15+*+*+*+*+?")
                .setBody()
                .constant("{ \"status\": \"OPEN\" }")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.twitterBcCollection}}&operation=findAll")
                .bean(employeeTwitterSimulationProcessor,
                        "closeTwitterBusinessCase")
                .wireTap("direct:updateTwitterBusinessCaseMongo")
                //.setHeader("CamelTwitter.output", header("CamelTwitter.message"))
                .to("twitter://timeline/user?consumerKey={{twitter.consumer.key}}&"
                        + "consumerSecret={{twitter.consumer.secret}}&accessToken={{twitter.access.token}}&"
                        + "accessTokenSecret={{twitter.access.token.secret}}")
                 .log(LoggingLevel.INFO, org.slf4j.LoggerFactory.getLogger("CustomRouteBuilder.class"), "Twitter-request was answered and closed")
                .end();

        /**
         * Updates the TwitterBusinessCase in the mongoDB
         */
        from("direct:updateTwitterBusinessCaseMongo")
                .setBody(header("bc"))
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.twitterBcCollection}}&operation=save")
                .end();

        /**
         * An employee answers and closes an OPEN MailBusinessCase every 60
         * seconds
         */
        from("quartz2://employeeGroup/answerMailTimer?cron=0/60+*+*+*+*+?")
                .setBody()
                .constant("{ \"status\": \"OPEN\" }")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.mailBcCollection}}&operation=findAll")
                .bean(employeeMailSimulationProcessor,
                        "answerMailBusinessCase")
                .wireTap("direct:updateMailBusinessCaseMongo")
                .to("smtps://{{mail.smtp.address}}:{{mail.smtp.port}}?password={{mail.password}}&username={{mail.userName}}")
                .log(LoggingLevel.INFO, org.slf4j.LoggerFactory.getLogger("CustomRouteBuilder.class"), "Mail-request was answered and closed")
                .end();

        /**
         * Updates the MailBusinessCase in the mongoDB
         */
        from("direct:updateMailBusinessCaseMongo")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.mailBcCollection}}&operation=save")
                .end();
    }
}
