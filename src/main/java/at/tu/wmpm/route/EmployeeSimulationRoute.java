package at.tu.wmpm.route;

import at.tu.wmpm.processor.CalendarProcessor;
import at.tu.wmpm.processor.EmployeeFacebookSimulationProcessor;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=findAll")
                .bean(EmployeeFacebookSimulationProcessor.class,
                        "commentOnFacebookBusinessCase")
                .to("facebook://commentPost?postId="
                        + header("CamelFacebook.postId") + "&" + "message="
                        + header("CamelFacebook.message")).end();

        /**
         * An employee closes a Facebook ticket (aka Facebook post or
         * FacebookBusinessCase) if there are more than 1 available, every 55
         * second (includes leaving a comment before deleting the post)
         * 
         * TODO Think about the reasonableness of closing a FacebookBusinessCase
         * (deleting a Post)
         */
        from("quartz2://employeeGroup/closeFacebookTimer?cron=0/55+*+*+*+*+?")
                .to("mongodb:mongo?database={{mongodb.database}}&collection={{mongodb.collection}}&operation=findAll")
                .bean(EmployeeFacebookSimulationProcessor.class,
                        "closeFacebookBusinessCase")
                .to("facebook://commentPost?postId="
                        + header("CamelFacebook.postId") + "&" + "message="
                        + header("CamelFacebook.message")).end();
    }
}