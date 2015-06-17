package at.tu.wmpm.route;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import at.tu.wmpm.processor.EmployeeFacebookSimulationProcessor;
import at.tu.wmpm.processor.EmployeeMailSimulationProcessor;

/**
 * Simulation for the employee
 *
 * @author Christian
 *
 */
@Component
public class EmployeeSimulationRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory
            .getLogger(EmployeeSimulationRoute.class);

    @Override
    @SuppressWarnings("deprecation")
    public void configure() throws Exception {

        /**
         * An employee answers (aka adds comment) to a Facebook businessCase
         * every 45 seconds
         */
        from("quartz2://employeeGroup/commentFacebookTimer?cron=0/45+*+*+*+*+?")
                .setBody()
                .constant("{ \"status\": \"OPEN\" }")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.facebookBcCollection}}&operation=findAll")
                .bean(EmployeeFacebookSimulationProcessor.class,
                        "commentOnFacebookBusinessCase")
                .to("facebook://commentPost?postId="
                        + header("CamelFacebook.postId") + "&" + "message="
                        + header("CamelFacebook.message")).end();

        /**
         * An employee closes a Facebook ticket (aka Facebook post or open
         * FacebookBusinessCase), every 55 seconds (includes leaving a comment)
         */
        from("quartz2://employeeGroup/closeFacebookTimer?cron=0/15+*+*+*+*+?")
                .setBody()
                .constant("{ \"status\": \"OPEN\" }")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.facebookBcCollection}}&operation=findAll")
                .bean(EmployeeFacebookSimulationProcessor.class,
                        "closeFacebookBusinessCase")
                .wireTap("direct:updateBusinessCaseMongo")
                .to("facebook://commentPost?postId="
                        + header("CamelFacebook.postId") + "&" + "message="
                        + header("CamelFacebook.message")).end();

        /**
         * Updates the business case in the mongoDB
         */
        from("direct:updateBusinessCaseMongo")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.facebookBcCollection}}&operation=save")
                .end();


        from("quartz2://employeeGroup/answerMailTimer?cron=0/60+*+*+*+*+?")
        .setBody()
        .constant("{ \"status\": \"OPEN\" }")
        .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.mailBcCollection}}&operation=findAll")
        .bean(EmployeeMailSimulationProcessor.class, "answerMailBusinessCase")
        .wireTap("direct:updateMailBusinessCaseMongo")
        .to("smtps://{{mail.smtp.address}}:{{mail.smtp.port}}?password={{mail.password}}&username={{mail.userName}}").end();

        from("direct:updateMailBusinessCaseMongo")
        .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.mailBcCollection}}&operation=save")
        .end();
    }
}