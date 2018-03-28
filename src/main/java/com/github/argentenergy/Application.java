package com.github.argentenergy;

import com.github.argentenergy.services.AnalyticsService;
import com.github.argentenergy.services.BrokerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The {@link Application} class is the entry point into running the application.
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private BrokerService brokerService;
    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Entry point to running the application.
     *
     * @param args Any command line arguments needed by the application.
     */
    public static void main(final String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    /**
     * Runs the application. The first step is to get the messages from the broker(s), and then run analytics on the
     * messages. After the analytics process finishes the data will be saved into the system.
     *
     * @param args Any command line arguments needed by the application.
     */
    @Override
    public void run(final String... args) throws Exception {
        analyticsService.run(brokerService.getMessages());
    }
}
