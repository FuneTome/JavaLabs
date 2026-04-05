import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

public class Main extends Application {

    public static class Table {
        private final DoubleProperty lowerLimit = new SimpleDoubleProperty();
        private final DoubleProperty upperLimit = new SimpleDoubleProperty();
        private final DoubleProperty steps = new SimpleDoubleProperty();
        private final ObjectProperty<Double> result = new SimpleObjectProperty<>();

        public Table(double lowerLimit, double upperLimit, double steps) {
            this.lowerLimit.set(lowerLimit);
            this.upperLimit.set(upperLimit);
            this.steps.set(steps);
            this.result.set(null);
        }

        public Table(double lowerLimit, double upperLimit, double steps, double result) {
            this.lowerLimit.set(lowerLimit);
            this.upperLimit.set(upperLimit);
            this.steps.set(steps);
            this.result.set(result);
        }

        public DoubleProperty lowerLimitProperty() { return lowerLimit; }
        public DoubleProperty upperLimitProperty() { return upperLimit; }
        public DoubleProperty stepsProperty() { return steps; }
        public ObjectProperty<Double> resultProperty() { return result; }

        public double getLowerLimit() { return lowerLimit.get(); }
        public double getUpperLimit() { return upperLimit.get(); }
        public double getSteps() { return steps.get(); }
        public Double getResult() { return result.get(); }
    }

    private TableView<Table> table = new TableView<>();
    private ObservableList<Table> items = FXCollections.observableArrayList();
    private ArrayList<RecIntegral> recIntegral = new ArrayList<>();
    private static final DoubleAdder totalRes = new DoubleAdder();

    @Override
    public void start(Stage primaryStage) {
        Button btnAdd = new Button("Добавить");
        Button btnDel = new Button("Удалить");
        Button btnCalc = new Button("Вычислить");
        Button btnClear = new Button("Очистить");
        Button btnFill = new Button("Заполнить");
        Button btnSaveText = new Button("Сохранить txt");
        Button btnLoadText = new Button("Загрузить txt");
        Button btnSaveBin = new Button("Сохранить bin");
        Button btnLoadBin = new Button("Загрузить bin");
        Button btnSaveJson = new Button("Сохранить json");
        Button btnLoadJson = new Button("Загрузить json");

        for (Button btn : new Button[]{btnAdd, btnDel, btnCalc}) { btn.setMaxWidth(Double.MAX_VALUE); }

        for (Button btn : new Button[]{btnClear, btnFill, btnSaveText, btnLoadText, btnSaveBin,
                btnLoadBin, btnSaveJson, btnLoadJson}) {
            btn.setPrefWidth(107);
        }

        Label label1 = new Label("Нижний предел");
        Label label2 = new Label("Верхний предел");
        Label label3 = new Label("Шаг");

        TextField field1 = new TextField();
        TextField field2 = new TextField();
        TextField field3 = new TextField();

        btnAdd.setOnAction(e -> {
            try{
                if (field1.getText().isBlank() || field2.getText().isBlank() || field3.getText().isBlank()) {
                    throw new InputException("Значение не было введено!");
                } else if (checkRange(field1.getText())){
                    throw new InputException("Неверный диапазон данных!\n Вы ввели : ", Double.parseDouble(field1.getText()));
                } else if (checkRange(field2.getText())){
                    throw new InputException("Неверный диапазон данных!\n Вы ввели : ", Double.parseDouble(field2.getText()));
                } else if (Double.parseDouble(field3.getText()) >= Double.parseDouble(field2.getText())){
                    throw new InputException("Шаг не должен быть больше верхнего лимита!\n Вы ввели : ", Double.parseDouble(field3.getText()));
                } else {
                    recIntegral.add(new RecIntegral(Double.parseDouble(field1.getText()),
                            Double.parseDouble(field2.getText()),
                            Double.parseDouble(field3.getText())));
                    items.add(recIntegral.getLast().getTable());
                }
            } catch (NumberFormatException exception){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Предупреждение!");
                alert.setHeaderText(exception.getMessage());
                alert.setContentText("Входные данные должны быть в числовом формате и в диапазоне от 0.000001 до 1000000");
                alert.showAndWait();
            } catch (InputException exception){
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Предупреждение!");
                alert.setHeaderText(exception.getMessage());
                alert.setContentText("Входные данные должны быть в диапазоне от 0.000001 до 1000000");
                alert.showAndWait();
            }
        });
        btnDel.setOnAction(e -> {
            Table selected = table.getSelectionModel().getSelectedItem();
            recIntegral.remove(selected);
            items.remove(selected);
        });
        btnCalc.setOnAction(e -> {
            RecIntegral selected = new RecIntegral(table.getSelectionModel().getSelectedItem());
            int index = recIntegral.indexOf(selected);

            double lower = selected.getLowerLimit();
            double upper = selected.getUpperLimit();
            double step = selected.getStep();            // шаг интегрирования, введённый пользователем
            int numberOfThreads = 7;                    // можно вынести в константу

            double totalLength = upper - lower;
            double partLength = totalLength / numberOfThreads;

            DoubleAdder partialSum = new DoubleAdder();  // локальный сумматор
            Thread[] threads = new Thread[numberOfThreads];

            for (int i = 0; i < numberOfThreads; i++) {
                double partStart = lower + i * partLength;
                double partEnd   = lower + (i + 1) * partLength;
                if (i == numberOfThreads - 1) partEnd = upper;

                final double start = partStart;
                final double end   = partEnd;

                threads[i] = new Thread(() -> {
                    double sum = 0.0;
                    double x = start;
                    while (x < end) {
                        double next = Math.min(x + step, end);
                        sum += (selected.f(x) + selected.f(next)) / 2.0 * (next - x);
                        x = next;
                    }
                    partialSum.add(sum);
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ex);
                }
            }
            double total = partialSum.sum();
            selected.setResult(total);
            items.set(index, selected.getTable());
        });
        btnClear.setOnAction(e -> {
            items.clear();
        });
        btnFill.setOnAction(e -> {
            items.clear();
            for (RecIntegral r : recIntegral) {
                items.add(r.getTable());
            }
        });
        btnSaveText.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить в текстовый файл");

            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome, "Downloads");
            fileChooser.setInitialDirectory(downloadsFolder);

            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    for (RecIntegral ri : recIntegral) {
                        writer.printf(Locale.US, "%f %f %f %f%n",
                                ri.getLowerLimit(),
                                ri.getUpperLimit(),
                                ri.getStep(),
                                ri.getResult());
                    }
                    Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
                    errorAlert.setTitle("Успех");
                    errorAlert.setHeaderText("Файл успешно сохранен");
                    errorAlert.showAndWait();
                } catch (IOException ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Не удалось сохранить файл");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
        btnLoadText.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Загрузить из текстового файла");
            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome, "Downloads");
            fileChooser.setInitialDirectory(downloadsFolder);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim().replace(",", ".");
                        if (line.isEmpty()) continue;
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 4) {
                            try {
                                double lower = Double.parseDouble(parts[0]);
                                double upper = Double.parseDouble(parts[1]);
                                double step = Double.parseDouble(parts[2]);
                                double result = Double.parseDouble(parts[3]);

                                RecIntegral ri = new RecIntegral(lower, upper, step);
                                ri.setResult(result);
                                recIntegral.add(ri);
                                items.add(ri.getTable());
                            } catch (NumberFormatException ex) {
                                // Пропускаем некорректные строки
                            }
                        }
                    }
                } catch (IOException ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Не удалось загрузить файл");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
        btnSaveBin.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить в бинарный файл");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Бинарные файлы", "*.bin"));

            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome, "Downloads");
            if (downloadsFolder.exists()) {
                fileChooser.setInitialDirectory(downloadsFolder);
            }

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try (ObjectOutputStream oos = new ObjectOutputStream(
                        new BufferedOutputStream(new FileOutputStream(file)))) {
                    oos.writeObject(recIntegral);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Успех");
                    successAlert.setHeaderText("Файл успешно сохранён");
                    successAlert.setContentText("Путь: " + file.getAbsolutePath());
                    successAlert.showAndWait();
                } catch (IOException ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Не удалось сохранить файл");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
        btnLoadBin.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Загрузить из бинарного файла");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Бинарные файлы", "*.bin"));

            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome, "Downloads");
            if (downloadsFolder.exists()) {
                fileChooser.setInitialDirectory(downloadsFolder);
            }

            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try (ObjectInputStream ois = new ObjectInputStream(
                        new BufferedInputStream(new FileInputStream(file)))) {
                    ArrayList<RecIntegral> loadedList = (ArrayList<RecIntegral>) ois.readObject();

                    for (RecIntegral ri : loadedList) {
                        recIntegral.add(ri);
                        items.add(ri.getTable());
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Успех");
                    successAlert.setHeaderText("Файл успешно загружен");
                    successAlert.setContentText("Добавлено записей: " + loadedList.size());
                    successAlert.showAndWait();
                } catch (IOException | ClassNotFoundException ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Не удалось загрузить файл");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
        btnSaveJson.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить в JSON файл");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON файлы", "*.json"));

            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome, "Downloads");
            if (downloadsFolder.exists()) {
                fileChooser.setInitialDirectory(downloadsFolder);
            }

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try (Writer writer = new FileWriter(file)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(recIntegral, writer);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Успех");
                    successAlert.setHeaderText("Файл успешно сохранён");
                    successAlert.setContentText("Путь: " + file.getAbsolutePath());
                    successAlert.showAndWait();
                } catch (IOException ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Не удалось сохранить файл");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });
        btnLoadJson.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Загрузить из JSON файла");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON файлы", "*.json"));

            String userHome = System.getProperty("user.home");
            File downloadsFolder = new File(userHome, "Downloads");
            if (downloadsFolder.exists()) {
                fileChooser.setInitialDirectory(downloadsFolder);
            }

            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try (Reader reader = new FileReader(file)) {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<ArrayList<RecIntegral>>(){}.getType();
                    ArrayList<RecIntegral> loadedList = gson.fromJson(reader, listType);

                    for (RecIntegral ri : loadedList) {
                        recIntegral.add(ri);
                        items.add(ri.getTable());
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Успех");
                    successAlert.setHeaderText("Файл успешно загружен");
                    successAlert.setContentText("Добавлено записей: " + loadedList.size());
                    successAlert.showAndWait();
                } catch (IOException ex) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Ошибка");
                    errorAlert.setHeaderText("Не удалось загрузить файл");
                    errorAlert.setContentText(ex.getMessage());
                    errorAlert.showAndWait();
                }
            }
        });

        HBox hbox = new HBox(10, btnAdd, btnDel, btnCalc);
        HBox hbox2 = new HBox(10, btnClear, btnFill);
        HBox hbox3 = new HBox(10, btnSaveText, btnLoadText);
        HBox hbox4 = new HBox(10, btnSaveBin, btnLoadBin);
        HBox hbox5 = new HBox(10, btnSaveJson, btnLoadJson);

        VBox buttonBox = new VBox(10, hbox, hbox2, hbox3, hbox4, hbox5);
        buttonBox.setPadding(new Insets(0, 0, 20, 20));

        VBox labelBox = new VBox(50, label1, label2, label3);
        VBox fieldBox = new VBox(40, field1, field2, field3);
        HBox formBox = new HBox(10, labelBox, fieldBox);

        TableColumn<Table, Double> lowerLimitCol = new TableColumn<>("Нижний предел");
        lowerLimitCol.setCellValueFactory(new PropertyValueFactory<>("lowerLimit"));

        TableColumn<Table, Double> upperLimitCol = new TableColumn<>("Верхний предел");
        upperLimitCol.setCellValueFactory(new PropertyValueFactory<>("upperLimit"));

        TableColumn<Table, Double> stepsCol = new TableColumn<>("Шаг интегрирования");
        stepsCol.setCellValueFactory(new PropertyValueFactory<>("steps"));

        TableColumn<Table, Double> resultCol = new TableColumn<>("Результат");
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));

        lowerLimitCol.setPrefWidth(150);
        upperLimitCol.setPrefWidth(150);
        stepsCol.setPrefWidth(150);
        resultCol.setPrefWidth(250);

        table.getColumns().addAll(lowerLimitCol, upperLimitCol, stepsCol, resultCol);
        table.setItems(items);

        BorderPane root = new BorderPane();
        root.setRight(buttonBox);
        root.setLeft(formBox);
        root.setBottom(table);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 700, 550);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Функция: 1/ln(x)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean checkRange(String a){
        double b = Math.abs(Double.parseDouble(a));
        return b < 0.000001 || b > 1000000;
    }

    public static void main(String[] args) {
        launch(args);
    }
}