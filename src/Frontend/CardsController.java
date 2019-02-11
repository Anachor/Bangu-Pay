package Frontend;


import Common.Card;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CardsController implements Initializable {
    @FXML
    private Text phoneNo;
    @FXML
    private TableColumn cardColumn;
    @FXML
    private TableColumn typeColumn;
    @FXML
    private TableView table;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        phoneNo.setText(Main.session.getPhone());
        cardColumn.setCellValueFactory(new PropertyValueFactory<>("cardNo"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("cardType"));

        List<Card> cards = Main.session.getCardsForNexusAccount(Main.session.getPhone());
        System.out.println(cards.size());

        for (Card card: cards) {
            table.getItems().add(card);
            System.out.println(card.getCardNo() + " " + card.getCardType());
        }
    }

}
