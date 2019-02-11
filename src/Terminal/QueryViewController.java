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
    Button runButton;
    @FXML
    TableView<List<StringProperty>> resultsTable;
    @FXML
    ListView<Hyperlink> simpleQuery;
    @FXML
    ListView <Hyperlink> complexQuery;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeArea.setText("Write SQL here");
        //addQueries();
    }

    @FXML
    public void RunQuery() {
        try {
            String SQL = codeArea.getText();
            ResultSet rs = Main.session.SQLQuery(SQL);
            ResultSetMetaData rsmd = rs.getMetaData();
            resultsTable.getColumns().clear();

            int n = rsmd.getColumnCount();
            for (int i=1; i<=n; i++) {
                String name = rsmd.getColumnName(i);
                TableColumn<List<StringProperty>, String> column = new TableColumn(name);
                int finalI = i - 1;
                column.setCellValueFactory(data -> data.getValue().get(finalI));
                resultsTable.getColumns().add(column);
            }

            ObservableList<List<StringProperty>> data = FXCollections.observableArrayList();
            while (rs.next()) {
                List<StringProperty> firstRow = new ArrayList<>();
                for (int i=1; i<=n; i++) {
                    firstRow.add(i-1, new SimpleStringProperty(rs.getString(i)));
                }
                data.add(firstRow);
            }
            resultsTable.setItems(data);

        } catch (SQLException e) {
            Alerter.showAlert(Alert.AlertType.ERROR, runButton.getScene().getWindow(),
                    "Invalid SQLQuery", "The given SQL SQLQuery is invalid");
            e.printStackTrace();
        }
    }

    private void addQueries() {
        List<SQLQuery> queries = new ArrayList<>();
        queries.add(new SQLQuery("Show Services", "select * from services;"));
        queries.add(new SQLQuery("Show Persons", "select * from persons;"));
    }
}
