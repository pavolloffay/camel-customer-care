package at.tu.wmpm;

import org.apache.camel.spring.Main;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Customer care main App
 *
 */
public class MainApp {

    public static void main(String[] args) throws Exception {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfiguration.class);

        Main main = new Main();
        main.setApplicationContext(context);
        main.enableHangupSupport();

        /**
         * Run - all configuration is done in camel-context.xml
         * not passing arguments
         */
        main.run(new String[0]);
    }
}
