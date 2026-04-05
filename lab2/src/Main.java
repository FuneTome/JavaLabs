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
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.ArrayList;

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
            double low = Double.parseDouble(field1.getText());
            double up = Double.parseDouble(field2.getText());
            double st = Double.parseDouble(field3.getText());
            Table t = new Table(low, up, st, 0);
            RecIntegral rec = new RecIntegral(t);
            recIntegral.add(rec);
            items.add(t);
        });
        btnDel.setOnAction(e -> {
            Table selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                RecIntegral rec = recIntegral.stream()
                        .filter(r -> r.getTable() == selected)
                        .findFirst()
                        .orElse(null);
                if (rec != null) recIntegral.remove(rec);
                items.remove(selected);
            }
        });
        btnCalc.setOnAction(e -> {
            Table selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                RecIntegral rec = recIntegral.stream()
                        .filter(r -> r.getTable() == selected)
                        .findFirst()
                        .orElse(null);
                if (rec != null) {
                    rec.result();
                    table.refresh();
                }
            }
        });
        btnClear.setOnAction(e -> {
            items.clear();
            recIntegral.clear();
        });
        btnFill.setOnAction(e -> {
            items.clear();
            for (RecIntegral r : recIntegral) {
                items.add(r.getTable());
            }
        });

        VBox buttonBox = new VBox(10, btnAdd, btnDel, btnCalc, btnClear, btnFill);
        buttonBox.setPadding(new Insets(0, 0, 0, 20));

        VBox labelBox = new VBox(50, label1, label2, label3);
        VBox fieldBox = new VBox(40, field1, field2, field3);
        HBox formBox = new HBox(10, labelBox, fieldBox);

        table.setEditable(true);

        TableColumn<Table, Double> lowerLimitCol = new TableColumn<>("Нижний предел");
        lowerLimitCol.setCellValueFactory(new PropertyValueFactory<>("lowerLimit"));
        lowerLimitCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        lowerLimitCol.setOnEditCommit(event -> {
            Table row = event.getRowValue();
            row.lowerLimitProperty().set(event.getNewValue());
            row.resultProperty().set(null);
        });

        TableColumn<Table, Double> upperLimitCol = new TableColumn<>("Верхний предел");
        upperLimitCol.setCellValueFactory(new PropertyValueFactory<>("upperLimit"));
        upperLimitCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        upperLimitCol.setOnEditCommit(event -> {
            Table row = event.getRowValue();
            row.upperLimitProperty().set(event.getNewValue());
            row.resultProperty().set(null);
        });

        TableColumn<Table, Double> stepsCol = new TableColumn<>("Шаг интегрирования");
        stepsCol.setCellValueFactory(new PropertyValueFactory<>("steps"));
        stepsCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        stepsCol.setOnEditCommit(event -> {
            Table row = event.getRowValue();
            row.stepsProperty().set(event.getNewValue());
            row.resultProperty().set(null);
        });

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

    public static void main(String[] args) {
        launch(args);
    }
}