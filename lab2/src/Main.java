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
import javafx.stage.Stage;

public class Main extends Application {

    // Модель данных
    public static class Table {
        private final DoubleProperty lowerLimit = new SimpleDoubleProperty();
        private final DoubleProperty upperLimit = new SimpleDoubleProperty();
        private final DoubleProperty steps = new SimpleDoubleProperty();
        private final ObjectProperty<Double> result = new SimpleObjectProperty<>(null);

        public Table(String lowerLimit, String upperLimit, String steps) {
            this.lowerLimit.set(Double.parseDouble(lowerLimit));
            this.upperLimit.set(Double.parseDouble(upperLimit));
            this.steps.set(Double.parseDouble(steps));
        }

        public DoubleProperty lowerLimitProperty() { return lowerLimit; }
        public DoubleProperty upperLimitProperty() { return upperLimit; }
        public DoubleProperty stepsProperty() { return steps; }
        public ObjectProperty<Double> resultProperty() { return result; }
    }

    private TableView<Table> table = new TableView<>();
    private ObservableList<Table> items = FXCollections.observableArrayList();
    private RecIntegral recIntegral;
    private boolean isLast = false; //Переменная нужна, чтобы не добавлять последнюю запись
                                // которая еще существует в таблице

    @Override
    public void start(Stage primaryStage) {
        Button btnAdd = new Button("Добавить");
        Button btnDel = new Button("Удалить");
        Button btnCalc = new Button("Вычислить");
        Button btnClear = new Button("Очистить");
        Button btnFill = new Button("Заполнить");

        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnDel.setMaxWidth(Double.MAX_VALUE);
        btnCalc.setMaxWidth(Double.MAX_VALUE);
        btnClear.setMaxWidth(Double.MAX_VALUE);
        btnFill.setMaxWidth(Double.MAX_VALUE);

        Label label1 = new Label("Нижний предел");
        Label label2 = new Label("Верхний предел");
        Label label3 = new Label("Шаг");

        TextField field1 = new TextField();
        TextField field2 = new TextField();
        TextField field3 = new TextField();

        btnAdd.setOnAction(e -> {
            items.add(new Table(field1.getText(), field2.getText(), field3.getText()));
            recIntegral = new RecIntegral(new Table(field1.getText(), field2.getText(), field3.getText()));
        });
        btnDel.setOnAction(e -> {
            Table selected = table.getSelectionModel().getSelectedItem();
            items.remove(selected);
            isLast = true;
        });
        btnCalc.setOnAction(e -> {
            Table selected = table.getSelectionModel().getSelectedItem();
            double a = selected.lowerLimit.get();
            double b = selected.upperLimit.get();
            double step = selected.steps.get();
            double res = result(a, b, step);
            selected.result.set(res);
        });
        btnClear.setOnAction(e -> {
            items.clear();
            isLast = true;
        });
        btnFill.setOnAction(e -> {
            if(isLast){
                items.add(recIntegral.getLastRecords());
                isLast = false;
            }
        });

        VBox buttonBox = new VBox(10, btnAdd, btnDel, btnCalc, btnClear, btnFill);
        buttonBox.setPadding(new Insets(0, 0, 0, 20));

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

    public double result(double a, double b, double step) {
        double res = 0;
        double ai = 0;
        for (ai = (a + step); ai <= b; ai += step) {
            res += (((f(a) + f(ai)) / 2) * step);
            a += step;
        }
        res += (((f(ai) + f(b)) / 2) * ((b - a) % step));
        return res;
    }

    public double f(double x){
        return 1/Math.log(x);
    }

    public static void main(String[] args) {
        launch(args);
    }
}