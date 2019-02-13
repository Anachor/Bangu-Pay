package Terminal;

import Common.Alerter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class QueryViewController implements Initializable {
    @FXML
    TextArea codeArea;
    @FXML
    TableView<List<StringProperty>> resultsTable;
    @FXML
    ListView simpleQuery;
    @FXML
    ListView  complexQuery;
    @FXML
    ListView functions;
    @FXML
    ListView triggers;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeArea.setText("Write SQL here");
        addSimpleQueries();
        addComplexQueries();
        addFunction();
        addTriggers();
    }

    public void executeQuery(String SQL, boolean supress) throws SQLException {
        SQL = SQL.trim();
        if (SQL.equals("")) return;
        PreparedStatement ps = Main.session.SQLQuery(SQL);
        ps.execute();

        int updates = ps.getUpdateCount();
        if (updates != -1) {
            if (supress) return;
            if (updates == 0) {
                Alerter.showAlert(Alert.AlertType.INFORMATION, codeArea.getScene().getWindow(),
                        "Completed", "Operation Successful");
            } else {
                Alerter.showAlert(Alert.AlertType.INFORMATION, codeArea.getScene().getWindow(),
                        "Table Updated", updates + " Rows Updated.");
            }
            return;
        }
        ResultSet rs = ps.getResultSet();

        ResultSetMetaData rsmd = rs.getMetaData();
        resultsTable.getColumns().clear();

        int n = rsmd.getColumnCount();
        for (int i = 1; i <= n; i++) {
            String name = rsmd.getColumnName(i);
            TableColumn<List<StringProperty>, String> column = new TableColumn(name);
            int finalI = i - 1;
            column.setCellValueFactory(data -> data.getValue().get(finalI));
            resultsTable.getColumns().add(column);
        }

        ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
        while (rs.next()) {
            List<StringProperty> firstRow = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                firstRow.add(i - 1, new SimpleStringProperty(rs.getString(i)));
            }
            data.add(firstRow);
        }
        resultsTable.setItems(data);
    }

    @FXML
    public void runAll() {
        try {
            String txt = codeArea.getText().trim();

            String[] ss = txt.split("===");
            txt = ss[ss.length-1];

            ss = txt.split(";");

            for (String SQL: ss) {
                executeQuery(SQL, false);
            }

        } catch (SQLException e) {
            Alerter.showAlert(Alert.AlertType.ERROR, codeArea.getScene().getWindow(),
                    "Invalid SQLQuery", "The given SQL Query is invalid");
            e.printStackTrace();
        }
    }

    @FXML
    public void runSelected() {
        try {
            String txt = codeArea.getSelectedText().trim();

            String[] ss = txt.split("===");
            txt = ss[ss.length-1];

            ss = txt.split(";");

            for (String SQL: ss) {
                executeQuery(SQL, false);
            }

        } catch (SQLException e) {
            Alerter.showAlert(Alert.AlertType.ERROR, codeArea.getScene().getWindow(),
                    "Invalid SQLQuery", "The given SQL Query is invalid");
            e.printStackTrace();
        }
    }


    private void addSimpleQueries() {
        for (SQLQuery query: Queries.getSimpleQueries()) {
            Hyperlink hyperlink = new Hyperlink();
            hyperlink.setText(query.Name);
            hyperlink.setOnAction(e -> {
                codeArea.setText(query.SQL);
            });

            simpleQuery.getItems().add(hyperlink);
        }
    }

    private void addComplexQueries() {
        for (SQLQuery query: Queries.getComplexQueries()) {
            Hyperlink hyperlink = new Hyperlink();
            hyperlink.setText(query.Name);
            hyperlink.setOnAction(e -> {
                codeArea.setText(query.SQL);
            });

            complexQuery.getItems().add(hyperlink);
        }
    }

    private void addFunction() {
        for (SQLQuery query: Queries.getFunctions()) {
            Hyperlink hyperlink = new Hyperlink();
            hyperlink.setText(query.Name);
            hyperlink.setOnAction(e -> {
                codeArea.setText(query.SQL);
            });
            functions.getItems().add(hyperlink);
        }
    }


    private void addTriggers() {
        for (SQLQuery query: Queries.getTriggers()) {
            Hyperlink hyperlink = new Hyperlink();
            hyperlink.setText(query.Name);
            hyperlink.setOnAction(e -> {
                codeArea.setText(query.SQL);
            });
            triggers.getItems().add(hyperlink);
        }
    }
}
