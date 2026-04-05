import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MyApp extends Application {

    @Override
    public void start(Stage stage) {
        // Прогресс-бар с определённым значением 30%
        ProgressBar progressBar = new ProgressBar(0.3);
        progressBar.setPrefWidth(200);

        // Индикатор прогресса с определённым значением 70%
        ProgressIndicator indicator = new ProgressIndicator(0.7);
        indicator.setPrefSize(50, 50);

        // Неопределённый режим (индикатор вращается)
        ProgressBar indeterminateBar = new ProgressBar();
        indeterminateBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        indeterminateBar.setPrefWidth(200);

        // Кнопка для имитации изменения прогресса
        Button updateButton = new Button("Увеличить прогресс");
        updateButton.setOnAction(e -> {
            double newProgress = progressBar.getProgress() + 0.1;
            if (newProgress > 1.0) newProgress = 0.0;
            progressBar.setProgress(newProgress);
            indicator.setProgress(newProgress);
        });

        VBox root = new VBox(15, progressBar, indicator, indeterminateBar, updateButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 300, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}