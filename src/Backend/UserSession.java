package Backend;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Common.Card;


public class UserSession {

    private final String url = "jdbc:postgresql://localhost/NexusPay";
    private final String user = "postgres";
    private final String password = "123";
    private Connection conn;

    private String phone;

    public String getPhone() {
        return phone;
    }

    boolean active = false;

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public boolean login(String phoneno, String password) {
        try {
            active = true;
            String sql = "select * from nexusaccounts where phone_no = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, phoneno);

            ResultSet rs = ps.executeQuery();
            String hashedPassword;
            if (rs.next())  hashedPassword = rs.getString("password");
            else return false;

            CallableStatement cs = conn.prepareCall("{? = call crypt (?, ?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2,password);
            cs.setString(3, hashedPassword);
            cs.execute();
            String hashedUserPassword = cs.getString(1);
            if (hashedPassword.equals(hashedUserPassword)) {
                active = true;
                phone = phoneno;
                return true;
            }
            else return false;
        } catch (SQLException e) {
            System.out.println("SQL Exception");
            e.printStackTrace();
            return false;
        }
    }

    public void logout() {
        phone = null;
    }

    public UserSession() {
        try {
            this.conn = connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Card> getCardsForNexusAccount(String phoneNo) {
        List<Card>  cards = new ArrayList<Card>();
        try {
            String sql = "select cards.card_no, sub_class_type\n" +
                    "from account_cards join cards on account_cards.card_no = cards.card_no\n" +
                    "where phone_no = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, phoneNo);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Card card = new Card(rs.getString("card_no"), rs.getString("sub_class_type"));
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }


}
