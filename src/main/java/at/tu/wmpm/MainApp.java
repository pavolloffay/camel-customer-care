package at.tu.wmpm;

import org.apache.camel.spring.Main;


/**
 * main class doesn't work because spring beans are injected to MailRouter so every class
 * mas to be managed by DI container
 */
public class MainApp {

    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.enableHangupSupport();

        //adds MailRouter
        main.addRouteBuilder(new MailRouter());
        
        //adds FacebookRouter
        main.addRouteBuilder(new FacebookRouter());
        
        main.run(args);
    }
}
