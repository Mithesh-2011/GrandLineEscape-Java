package com.grandlineescape;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    ApplicationRunner openBrowser(Environment environment) {
        return (ApplicationArguments arguments) -> {
            if (!Boolean.parseBoolean(environment.getProperty("OPEN_BROWSER", "true"))
                    || !Desktop.isDesktopSupported()
                    || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                return;
            }

            String port = environment.getProperty("local.server.port",
                    environment.getProperty("server.port", "8080"));
            try {
                Desktop.getDesktop().browse(URI.create("http://localhost:" + port));
            } catch (IOException exception) {
                System.err.println("Could not open a browser: " + exception.getMessage());
            }
        };
    }
}
