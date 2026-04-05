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
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.converter.DoubleStringConverter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.DoubleAdder;

public class Main extends Application {
    public static class Table {
        private final DoubleProperty lowerLimit = new SimpleDoubleProperty();
        private final DoubleProperty upperLimit = new SimpleDoubleProperty();
        private final DoubleProperty steps = new SimpleDoubleProperty();
        private final ObjectProperty<Double> result = new SimpleObjectProperty<>();
        private RecIntegral recIntegral;

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

        public void setRecIntegral(RecIntegral ri) { this.recIntegral = ri; }
        public RecIntegral getRecIntegral() { return recIntegral; }
    }

    private static class IntegralRunnable implements Runnable {
        private final double start;
        private final double end;
        private final double step;
        private final DoubleAdder adder;
        private final RecIntegral integral; // для вызова f(x)

        public IntegralRunnable(double start, double end, double step, DoubleAdder adder, RecIntegral integral) {
            this.start = start;
            this.end = end;
            this.step = step;
            this.adder = adder;
            this.integral = integral;
        }

        @Override
        public void run() {
            double sum = 0.0;
            double x = start;
            while (x < end) {
                double next = Math.min(x + step, end);
                sum += (integral.f(x) + integral.f(next)) / 2.0 * (next - x);
                x = next;
            }
            adder.add(sum);
        }
    }

    private TableView<Table> tableView = new TableView<>();
    private ObservableList<Table> items = FXCollections.observableArrayList();
    private ArrayList<RecIntegral> recIntegral = new ArrayList<>();

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
            try {
                if (field1.getText().isBlank() || field2.getText().isBlank() || field3.getText().isBlank()) {
                    throw new InputException("Значение не было введено!");
                }
                if (checkRange(field1.getText())) {
                    throw new InputException("Неверный диапазон данных!\n Вы ввели : ", Double.parseDouble(field1.getText()));
                }
                if (checkRange(field2.getText())) {
                    throw new InputException("Неверный диапазон данных!\n Вы ввели : ", Double.parseDouble(field2.getText()));
                }
                if (Double.parseDouble(field3.getText()) >= Double.parseDouble(field2.getText())) {
                    throw new InputException("Шаг не должен быть больше верхнего лимита!\n Вы ввели : ", Double.parseDouble(field3.getText()));
                }

                double low = Double.parseDouble(field1.getText());
                double up = Double.parseDouble(field2.getText());
                double st = Double.parseDouble(field3.getText());

                RecIntegral ri = new RecIntegral(low, up, st);
                Table row = new Table(low, up, st);
                row.setRecIntegral(ri);
                recIntegral.add(ri);
                items.add(row);
            } catch (NumberFormatException ex) {
                showAlert("Ошибка формата", "Входные данные должны быть числами!\n" + ex.getMessage());
            } catch (InputException ex) {
                showAlert("Ошибка ввода", ex.getMessage() + "\nДопустимый диапазон: 0.000001 ... 1000000");
            }
        });
        btnDel.setOnAction(e -> {
            Table selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                RecIntegral ri = selected.getRecIntegral();
                recIntegral.remove(ri);
                items.remove(selected);
            }
        });
        btnCalc.setOnAction(e -> {
            Table selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                RecIntegral ri = selected.getRecIntegral();
                if (ri != null) {
                    double lower = ri.getLowerLimit();
                    double upper = ri.getUpperLimit();
                    double step = ri.getStep();
                    int numberOfThreads = 7;

                    double totalLength = upper - lower;
                    double partLength = totalLength / numberOfThreads;

                    DoubleAdder partialSum = new DoubleAdder();
                    Thread[] threads = new Thread[numberOfThreads];

                    for (int i = 0; i < numberOfThreads; i++) {
                        double partStart = lower + i * partLength;
                        double partEnd = lower + (i + 1) * partLength;
                        if (i == numberOfThreads - 1) partEnd = upper;

                        Runnable task = new IntegralRunnable(partStart, partEnd, step, partialSum, ri);
                        threads[i] = new Thread(task);
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
                    ri.setResult(total);
                    selected.resultProperty().set(total);
                    tableView.refresh();
                }
            }
        });
        btnClear.setOnAction(e -> {
            items.clear();
            recIntegral.clear();
        });
        btnFill.setOnAction(e -> {
            items.clear();
            for (RecIntegral ri : recIntegral) {
                Table row = new Table(ri.getLowerLimit(), ri.getUpperLimit(), ri.getStep(), ri.getResult());
                row.setRecIntegral(ri);
                items.add(row);
            }
        });
        btnSaveText.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Сохранить в текстовый файл");
            fc.setInitialDirectory(new File(System.getProperty("user.home"), "Downloads"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
            File file = fc.showSaveDialog(primaryStage);
            if (file != null) {
                try (PrintWriter pw = new PrintWriter(file)) {
                    for (RecIntegral ri : recIntegral) {
                        pw.printf(Locale.US, "%f %f %f %f%n",
                                ri.getLowerLimit(), ri.getUpperLimit(), ri.getStep(), ri.getResult());
                    }
                    showAlert("Успех", "Файл сохранён: " + file.getAbsolutePath());
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось сохранить файл\n" + ex.getMessage());
                }
            }
        });
        btnLoadText.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Загрузить из текстового файла");
            fc.setInitialDirectory(new File(System.getProperty("user.home"), "Downloads"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));
            File file = fc.showOpenDialog(primaryStage);
            if (file != null) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim().replace(",", ".");
                        if (line.isEmpty()) continue;
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 4) {
                            try {
                                double low = Double.parseDouble(parts[0]);
                                double up = Double.parseDouble(parts[1]);
                                double step = Double.parseDouble(parts[2]);
                                double res = Double.parseDouble(parts[3]);
                                RecIntegral ri = new RecIntegral(low, up, step);
                                ri.setResult(res);
                                Table row = new Table(low, up, step, res);
                                row.setRecIntegral(ri);
                                recIntegral.add(ri);
                                items.add(row);
                            } catch (NumberFormatException ignored) { }
                        }
                    }
                    showAlert("Успех", "Данные загружены");
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось загрузить файл\n" + ex.getMessage());
                }
            }
        });
        btnSaveBin.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Сохранить в бинарный файл");
            fc.setInitialDirectory(new File(System.getProperty("user.home"), "Downloads"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Бинарные файлы", "*.bin"));
            File file = fc.showSaveDialog(primaryStage);
            if (file != null) {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                    oos.writeObject(recIntegral);
                    showAlert("Успех", "Бинарный файл сохранён");
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось сохранить\n" + ex.getMessage());
                }
            }
        });
        btnLoadBin.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Загрузить из бинарного файла");
            fc.setInitialDirectory(new File(System.getProperty("user.home"), "Downloads"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Бинарные файлы", "*.bin"));
            File file = fc.showOpenDialog(primaryStage);
            if (file != null) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    ArrayList<RecIntegral> loaded = (ArrayList<RecIntegral>) ois.readObject();
                    for (RecIntegral ri : loaded) {
                        Table row = new Table(ri.getLowerLimit(), ri.getUpperLimit(), ri.getStep(), ri.getResult());
                        row.setRecIntegral(ri);
                        recIntegral.add(ri);
                        items.add(row);
                    }
                    showAlert("Успех", "Загружено записей: " + loaded.size());
                } catch (IOException | ClassNotFoundException ex) {
                    showAlert("Ошибка", "Не удалось загрузить\n" + ex.getMessage());
                }
            }
        });
        btnSaveJson.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Сохранить в JSON");
            fc.setInitialDirectory(new File(System.getProperty("user.home"), "Downloads"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON файлы", "*.json"));
            File file = fc.showSaveDialog(primaryStage);
            if (file != null) {
                try (Writer writer = new FileWriter(file)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(recIntegral, writer);
                    showAlert("Успех", "JSON сохранён");
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось сохранить\n" + ex.getMessage());
                }
            }
        });
        btnLoadJson.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Загрузить из JSON");
            fc.setInitialDirectory(new File(System.getProperty("user.home"), "Downloads"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON файлы", "*.json"));
            File file = fc.showOpenDialog(primaryStage);
            if (file != null) {
                try (Reader reader = new FileReader(file)) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<ArrayList<RecIntegral>>(){}.getType();
                    ArrayList<RecIntegral> loaded = gson.fromJson(reader, type);
                    for (RecIntegral ri : loaded) {
                        Table row = new Table(ri.getLowerLimit(), ri.getUpperLimit(), ri.getStep(), ri.getResult());
                        row.setRecIntegral(ri);
                        recIntegral.add(ri);
                        items.add(row);
                    }
                    showAlert("Успех", "Загружено записей: " + loaded.size());
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось загрузить\n" + ex.getMessage());
                }
            }
        });

        HBox hbox1 = new HBox(10, btnAdd, btnDel, btnCalc);
        HBox hbox2 = new HBox(10, btnClear, btnFill);
        HBox hbox3 = new HBox(10, btnSaveText, btnLoadText);
        HBox hbox4 = new HBox(10, btnSaveBin, btnLoadBin);
        HBox hbox5 = new HBox(10, btnSaveJson, btnLoadJson);
        VBox buttonBox = new VBox(10, hbox1, hbox2, hbox3, hbox4, hbox5);
        buttonBox.setPadding(new Insets(0, 0, 20, 20));

        VBox labelBox = new VBox(50, label1, label2, label3);
        VBox fieldBox = new VBox(40, field1, field2, field3);
        HBox formBox = new HBox(10, labelBox, fieldBox);

        tableView.setEditable(true);

        TableColumn<Table, Double> lowerLimitCol = new TableColumn<>("Нижний предел");
        lowerLimitCol.setCellValueFactory(new PropertyValueFactory<>("lowerLimit"));
        lowerLimitCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        lowerLimitCol.setOnEditCommit(event -> {
            Table row = event.getRowValue();
            double newVal = event.getNewValue();
            double currentUpper = row.getUpperLimit();
            if (newVal > 0 && Math.abs(newVal) >= 0.000001 && Math.abs(newVal) <= 1000000 && newVal < currentUpper) {
                row.lowerLimitProperty().set(newVal);
                RecIntegral ri = row.getRecIntegral();
                if (ri != null) {
                    ri.setLowerLimit(newVal);
                    ri.setResult(0.0);
                    row.resultProperty().set(null);
                }
            } else {
                showAlert("Ошибка ввода", "Недопустимое значение нижнего предела.\n" +
                        "Допустимый диапазон: 0.000001 ... 1000000, значение > 0,\n" +
                        "нижний предел должен быть меньше верхнего (" + currentUpper + ").");
                tableView.refresh();
            }
        });

        TableColumn<Table, Double> upperLimitCol = new TableColumn<>("Верхний предел");
        upperLimitCol.setCellValueFactory(new PropertyValueFactory<>("upperLimit"));
        upperLimitCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        upperLimitCol.setOnEditCommit(event -> {
            Table row = event.getRowValue();
            double newVal = event.getNewValue();
            double currentLower = row.getLowerLimit();
            double currentStep = row.getSteps();
            if (newVal > 0 && Math.abs(newVal) >= 0.000001 && Math.abs(newVal) <= 1000000 && newVal > currentLower && currentStep < newVal) {
                row.upperLimitProperty().set(newVal);
                RecIntegral ri = row.getRecIntegral();
                if (ri != null) {
                    ri.setUpperLimit(newVal);
                    ri.setResult(0.0);
                    row.resultProperty().set(null);
                }
            } else {
                showAlert("Ошибка ввода", "Недопустимое значение верхнего предела.\n" +
                        "Допустимый диапазон: 0.000001 ... 1000000, значение > 0,\n" +
                        "верхний предел должен быть больше нижнего (" + currentLower + ")\n" +
                        "и больше шага (" + currentStep + ").");
                tableView.refresh();
            }
        });

        TableColumn<Table, Double> stepsCol = new TableColumn<>("Шаг интегрирования");
        stepsCol.setCellValueFactory(new PropertyValueFactory<>("steps"));
        stepsCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        stepsCol.setOnEditCommit(event -> {
            Table row = event.getRowValue();
            double newVal = event.getNewValue();
            double currentUpper = row.getUpperLimit();
            if (newVal > 0 && Math.abs(newVal) >= 0.000001 && Math.abs(newVal) <= 1000000 && newVal < currentUpper) {
                row.stepsProperty().set(newVal);
                RecIntegral ri = row.getRecIntegral();
                if (ri != null) {
                    ri.setStep(newVal);
                    ri.setResult(0.0);
                    row.resultProperty().set(null);
                }
            } else {
                showAlert("Ошибка ввода", "Недопустимое значение шага.\n" +
                        "Допустимый диапазон: 0.000001 ... 1000000, шаг > 0,\n" +
                        "шаг должен быть меньше верхнего предела (" + currentUpper + ").");
                tableView.refresh();
            }
        });

        TableColumn<Table, Double> resultCol = new TableColumn<>("Результат");
        resultCol.setCellValueFactory(new PropertyValueFactory<>("result"));

        lowerLimitCol.setPrefWidth(150);
        upperLimitCol.setPrefWidth(150);
        stepsCol.setPrefWidth(150);
        resultCol.setPrefWidth(250);

        tableView.getColumns().addAll(lowerLimitCol, upperLimitCol, stepsCol, resultCol);
        tableView.setItems(items);

        BorderPane root = new BorderPane();
        root.setRight(buttonBox);
        root.setLeft(formBox);
        root.setBottom(tableView);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 700, 550);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Функция: 1/ln(x)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean checkRange(String a) {
        double b = Math.abs(Double.parseDouble(a));
        return b < 0.000001 || b > 1000000;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}