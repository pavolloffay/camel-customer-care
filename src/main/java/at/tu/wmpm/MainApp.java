package at.tu.wmpm;

import org.apache.camel.spring.Main;

import java.util.Collections;

/**
 * App used to run from IDE,
 * from command line use mvn camel:run
 */
public class MainApp {

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.setFileApplicationContextUri("classpath*:camel-context.xml");
        main.enableHangupSupport();

        /**
         * Run - all configuration is done in camel-context.xml
         * not passing arguments
         */
        main.run(Collections.emptyList().toArray(new String[0]));
    }
}
