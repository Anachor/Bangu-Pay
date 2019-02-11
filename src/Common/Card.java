package Common;

public class Card {
    String cardNo;
    String cardType;

    public Card(String cardNo, String cardType) {
        this.cardNo = cardNo;
        this.cardType = cardType;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getCardType() {
        return cardType;
    }
}
