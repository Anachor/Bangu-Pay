package Terminal;

import java.util.ArrayList;
import java.util.List;

public class Queries {
    public static List<SQLQuery> getSimpleQueries() {
        List<SQLQuery> queries = new ArrayList<>();
        queries.add(new SQLQuery("Show All Nexus Accounts",
                "select * from nexusaccounts"));
        queries.add(new SQLQuery("Show Services",
                "select * from services;"));
        queries.add(new SQLQuery("Show Billers",
                "select * from billers \n" + "where phone_no = '01521433575';"));
        queries.add(new SQLQuery("Get Nexus Account For Card",
                "select * from nexusaccounts where phone_no = '01913373406'"));
        queries.add(new SQLQuery("Show Offers",
                "select * from offers where card_no = 4611686018427388083"));
        queries.add(new SQLQuery("Show Local Offer Outlets",
                "select * from local_offer_outlets where local_offer_id = 7"));
        return queries;
    }

    public static List<SQLQuery> getComplexQueries() {
        List<SQLQuery> queries = new ArrayList<>();
        queries.add(new SQLQuery("Show Cards",
                "select cards.card_no, sub_class_type\n" +
                        "from account_cards join cards on account_cards.card_no = cards.card_no\n" +
                        "where phone_no = '01998561074';"));

        queries.add(new SQLQuery("Get Offers for outlet",
                "select *\n" +
                        "from offers \n" +
                        "where\n" +
                        "  card_no = 4611686018427388083 and (type = 'global'\n" +
                        "  or 534 in\n" +
                        "     (select transaction_method_id from local_offer_outlets loo where loo.local_offer_id = offer_id)\n" +
                        "  );"));

        queries.add(new SQLQuery("Get Nexus Account for Card",
                "select n.phone_no, name, email_id\n" +
                        "from account_cards\n" +
                        "  join nexusaccounts n on account_cards.phone_no = n.phone_no\n" +
                        "where card_no = 4611686018427388083;"));

        queries.add(new SQLQuery("Show Tranaction History (Card)",
                "select *\n" +
                        "  from transactions\n" +
                        "  where\n" +
                        "    (select transaction_method_id\n" +
                        "      from transaction_methods\n" +
                        "      where sub_class_primary_key = 4611686018427388083\n" +
                        "    ) in (\"from\", \"to\");\n"));

        queries.add(new SQLQuery("Show Tranaction History (Nexus Account)",
                "select *\n" +
                        "  from transactions\n" +
                        "  where exists\n" +
                        "    (select transaction_method_id\n" +
                        "      from transaction_methods\n" +
                        "      where sub_class_primary_key in (\n" +
                        "        select card_no\n" +
                        "        from account_cards\n" +
                        "         where phone_no = '01998561074'\n" +
                        "        and transaction_method_id in (\"from\", \"to\")\n" +
                        "      )\n" +
                        "    );"));

        return queries;
    }
}
