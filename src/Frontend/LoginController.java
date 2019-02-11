package Frontend;

import Backend.UserSession;
import Common.Alerter;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;


public class LoginController {
    @FXML
    private TextField phoneField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button signInButton;

    @FXML
    protected void signInAction() {
        Window owner = signInButton.getScene().getWindow();
        String phoneno = phoneField.getText();
        String password = passwordField.getText();

        boolean success = Main.session.login(phoneno, password);
        if(success) {
            Main.setScene("cards.fxml");
        }
        else {
            System.out.println("Unsuccessful");
            Alerter.showAlert(Alert.AlertType.ERROR, signInButton.getScene().getWindow(),
                    " Login Unsuccessful", "Username and Password don't match");
        }
    }
}

