package Backend;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;

import Common.Card;


public class UserSession {

    private static String url;
    private static String user;
    private static String password;
    private Connection conn;

    static {
        String filename = "src/Backend/config.txt";
        try {
            File file = new File(filename);
            Scanner sc  = new Scanner(file);
            url = sc.nextLine();
            user = sc.nextLine();
            password = sc.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

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

    public PreparedStatement SQLQuery(String SQL) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(SQL);
        System.out.println(ps);
        return ps;
    }

    public static void main(String[] args) throws SQLException {
        UserSession session = new UserSession();
        String sql = ("select ");
        PreparedStatement ps = session.conn.prepareStatement(sql);
        System.out.println(ps);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString(1) + rs.getString(2) + rs.getString(3) + rs.getString(4));
        }
    }

}


