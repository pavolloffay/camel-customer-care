package at.tu.wmpm.route;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import at.tu.wmpm.processor.FileAggregationStrategy;
import at.tu.wmpm.processor.FileProcessor;
import at.tu.wmpm.processor.WireTapLogDropbox;

/**
 * Created by pavol on 8.6.2015.
 */
@Component
public class BackupLogsRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory
            .getLogger(ExceptionRoute.class);

    @Value("${dropbox.auth.param}")
    private String DROPBOX_AUTH_PARAMETERS;

    @Autowired
    private FileAggregationStrategy faStrategy;
    @Autowired
    private WireTapLogDropbox wiretapDropbox;

    @Override
    @SuppressWarnings("deprecation")
    public void configure() throws Exception {

        /**
         * Backup Logs to dropbox every 30 seconds (interval currently set for
         * testing purposes)
         */
        from(
                "file:logs/workingdir?recursive=true&delete=false&scheduler=quartz2&scheduler.cron=0/30+*+*+*+*+?")
                .bean(FileProcessor.class, "process")
                .aggregate(constant(true), faStrategy)
                .completionFromBatchConsumer()
                .to("file:logs/forDropbox?fileName=forDropbox.txt")
                .recipientList(
                        simple("dropbox://put?"
                                + DROPBOX_AUTH_PARAMETERS
                                + "&uploadMode=add&localPath=logs/forDropbox/forDropbox.txt&remotePath=/logs/backup_log_${date:now:yyyyMMdd_HH-mm-SS}.txt"))
                .wireTap("seda:logDropbox", wiretapDropbox);

        from("seda:logDropbox?concurrentConsumers=3")
                .to("file:logs/workingdir/wiretap-logs/logDropbox?fileName=upload_${date:now:yyyyMMdd_HH-mm-SS}.log&flatten=true");
    }
}