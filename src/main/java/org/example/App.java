package org.example;

import org.example.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App
{
    public static void main(String[] args) {
        // Initialize Spring Application Context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Keep the application running to prevent JVM from exiting
        synchronized (App.class) {
            try {
                /*  Puts the main thread in an indefinite waiting state
                    This is necessary to keep the application running
                    (since we are not using a Spring Boot web server)   */
                App.class.wait();
            } catch (InterruptedException e) {
                // If the thread is interrupted (e.g., by Ctrl+C), restore the interrupt flag
                Thread.currentThread().interrupt();
            }
        }

        // Gracefully close the Spring application context when exiting
        context.close();
    }

}
