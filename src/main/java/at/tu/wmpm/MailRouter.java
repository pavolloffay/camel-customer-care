package at.tu.wmpm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * Created by pavol on 30.04.2015 Edited by christian on 07.05.2015
 */
public class MailRouter extends RouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(MailRouter.class);

	@PostConstruct
	public void postConstruct() {
		log.debug("Mail component initialized");
	}

	@Override
	public void configure() throws Exception {

		from(
				"pop3s://{{eMailUserName}}@{{eMailPOPAddress}}:{{eMailPOPPort}}?password={{eMailPassword}}")
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						String timeStamp = new SimpleDateFormat(
								"yyyy-MM-dd @ HH:mm:ss").format(Calendar
								.getInstance().getTime());
						CamelContext camel = new DefaultCamelContext();
						ProducerTemplate template = camel
								.createProducerTemplate();
						Map<String, Object> headers = new HashMap<String, Object>();
						Map<String, Object> inHeaders;

						log.debug(ReflectionToStringBuilder.toString(exchange));

						Message in = exchange.getIn();
						log.debug("Mail body:\n" + in.getBody(String.class)
								+ "\n");

						// AUTO REPLY
						String[] bodyLines = in.getBody(String.class).split(
								System.getProperty("line.separator"));
						String bodyUpdated = "";
						for (String x : bodyLines) {
							bodyUpdated += "> " + x + "\n";
						}

						inHeaders = in.getHeaders();
						headers.put("To", inHeaders.get("Return-Path"));
						headers.put("From", inHeaders.get("To"));
						headers.put("Subject", "We received your request");
						String body = "Dear customer,\nWe received your mail and are currently processing the information\n\nbest regards, customer suppoprt\n\n\n"
								+ "Original mail, received at "
								+ timeStamp
								+ "\n\n" + bodyUpdated;
						camel.addComponent("properties", new PropertiesComponent("myprop.properties"));
						template.sendBodyAndHeaders(
								"smtps://{{eMailSMTPAddress}}:{{eMailSMTPPort}}?password={{eMailPassword}}&username={{eMailUserName}}",
								body, headers);

						log.debug("Successfully sent mail to {}",
								inHeaders.get("Return-Path"));
					}
				});
	}
}