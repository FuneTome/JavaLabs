package labs;

import javafx.application.Application;
import labs.client.MainFX;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApp {
    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
        new Thread(() -> Application.launch(MainFX.class, args)).start();
    }
}
