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
    Hyperlink runHyperlink;
    @FXML
    TableView<List<StringProperty>> resultsTable;
    @FXML
    ListView simpleQuery;
    @FXML
    ListView  complexQuery;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        codeArea.setText("Write SQL here");
        addSimpleQueries();
        addComplexQueries();
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
            Alerter.showAlert(Alert.AlertType.ERROR, runHyperlink.getScene().getWindow(),
                    "Invalid SQLQuery", "The given SQL SQLQuery is invalid");
            e.printStackTrace();
        }
    }

    private void addSimpleQueries() {
        List<SQLQuery> queries = new ArrayList<>();
        queries.add(new SQLQuery("Show All Nexus Accounts",
                "select * from nexusaccounts"));
        queries.add(new SQLQuery("Show Services",
                "select * from services;"));
        queries.add(new SQLQuery("Show Billers",
                "select * from billers \n" + "where nexus_id = '01913373406';"));
        queries.add(new SQLQuery("Get Nexus Account",
                "select * from nexusaccounts where phone_no = '01913373406'"));
        queries.add(new SQLQuery("Show Offers",
                "select * from offers where card_no = 12"));
        queries.add(new SQLQuery("Show Local Offer Outlets",
                "select * from local_offer_outlets where local_offer_id = 7"));

        for (SQLQuery query: queries) {
            Hyperlink hyperlink = new Hyperlink();
            hyperlink.setText(query.Name);
            hyperlink.setOnAction(e -> {
                codeArea.setText(query.SQL);
            });

            simpleQuery.getItems().add(hyperlink);
        }
    }

    private void addComplexQueries() {
        List<SQLQuery> queries = new ArrayList<>();
        queries.add(new SQLQuery("Show Cards",
                "select cards.card_no, sub_class_type\n" +
                "from account_cards join cards on account_cards.card_no = cards.card_no\n" +
                "where phone_no = '01913373406';"));

        queries.add(new SQLQuery("Get Offers for outlet",
                "select *\n" +
                        "from offers join card_offers co on offers.offer_id = co.offer_no\n" +
                        "where\n" +
                        "  card_no = '37' and (type = 'global'\n" +
                        "  or '41' in\n" +
                        "     (select transaction_method_id from local_offer_outlets loo where loo.local_offer_id = co.offer_no)\n" +
                        "  )"));

        queries.add(new SQLQuery("Get Nexus Account",
                "select n.phone_no, name, email_id\n" +
                        "from account_cards\n" +
                        "  join nexusaccounts n on account_cards.phone_no = n.phone_no\n" +
                        "where card_no = '42'"));

        queries.add(new SQLQuery("Show Tranaction History (Card)",
                "select n.phone_no, name, email_id\n" +
                        "from account_cards\n" +
                        "  join nexusaccounts n on account_cards.phone_no = n.phone_no\n" +
                        "where card_no = '42'"));

        queries.add(new SQLQuery("Show Tranaction History (Nexus Account)",
                "select *\n" +
                        "  from transactions\n" +
                        "  where exists\n" +
                        "    (select transaction_method_id\n" +
                        "      from transaction_methods\n" +
                        "      where sub_class_primary_key in (\n" +
                        "        select card_no\n" +
                        "        from account_cards\n" +
                        "         where phone_no = '019215151'\n" +
                        "        and transaction_method_id in (\"from\", \"to\")\n" +
                        "      )\n" +
                        "    );"));



        for (SQLQuery query: queries) {
            Hyperlink hyperlink = new Hyperlink();
            hyperlink.setText(query.Name);
            hyperlink.setOnAction(e -> {
                codeArea.setText(query.SQL);
            });

            complexQuery.getItems().add(hyperlink);
        }
    }
}
