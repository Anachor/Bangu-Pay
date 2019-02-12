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

        queries.add(new SQLQuery("Show Transaction History (Card)",
                "select *\n" +
                        "  from transactions\n" +
                        "  where\n" +
                        "    (select transaction_method_id\n" +
                        "      from transaction_methods\n" +
                        "      where sub_class_primary_key = 4611686018427388083\n" +
                        "    ) in (\"from\", \"to\");\n"));

        queries.add(new SQLQuery("Show Transaction History (Nexus Account)",
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

    public static List<SQLQuery> getFunctions() {
        List<SQLQuery> queries = new ArrayList<>();
        queries.add(new SQLQuery("Create Nexus Account","-- PROCEDURE: public.create_nexus_account(character varying, text, text, text)\n" +
                "\n" +
                "-- DROP PROCEDURE public.create_nexus_account(character varying, text, text, text);\n" +
                "\n" +
                "CREATE OR REPLACE PROCEDURE public.create_nexus_account(\n" +
                "\tphone_no character varying,\n" +
                "\tname text,\n" +
                "\temail text,\n" +
                "\tpassword text)\n" +
                "LANGUAGE 'plpgsql'\n" +
                "\n" +
                "AS $BODY$begin\n" +
                "\tinsert into nexusaccounts(phone_no,name,email_id,password)\n" +
                "\tvalues(phone_no,name,email,crypt(password,gen_salt('bf')));\n" +
                "end;$BODY$;\n" +
                "===\n" +
                "call create_nexus_account('99999999998','Monkey','Monkey@gmail.com','Monkey');\n" +
                "select * from nexusaccounts;"));
        queries.add(new SQLQuery("Pay Bill","-- PROCEDURE: public.use_offer(bigint, bigint)\n" +
                "\n" +
                "-- DROP PROCEDURE public.use_offer(bigint, bigint);\n" +
                "\n" +
                "CREATE OR REPLACE PROCEDURE public.use_offer(\n" +
                "\toffer_no bigint,\n" +
                "\ttransaction_no bigint)\n" +
                "LANGUAGE 'plpgsql'\n" +
                "\n" +
                "AS $BODY$\n" +
                "declare\n" +
                "  cash_back                 money;\n" +
                "  max_amount                money;\n" +
                "  used                      money;\n" +
                "  transaction_money         money;\n" +
                "  source                    bigint;\n" +
                "  destination               bigint;\n" +
                "  rate                      numeric;\n" +
                "begin\n" +
                "  select cashback_percentage,\"limit\",used_so_far into rate,max_amount,used\n" +
                "  from offers where offer_id = offer_no;\n" +
                "\n" +
                "  select balance,\"from\",\"to\" into transaction_money,destination,source\n" +
                "  from transactions where  transaction_id = transaction_no;\n" +
                "\n" +
                "  cash_back = least(max_amount-used,(transaction_money::numeric*rate)::money);\n" +
                "\n" +
                "  perform make_transaction(source,destination,cash_back);\n" +
                "\n" +
                "  update  offers set used_so_far = used_so_far + cash_back where offer_id=offer_no;\n" +
                "\n" +
                "end;\n" +
                "$BODY$;\n" +
                "===\n" +
                "call use_offer(7,5);\n" +
                "select * from transactions;"));
        queries.add(new SQLQuery("Make Transaction","-- FUNCTION: public.make_transaction(bigint, bigint, money)\n" +
                "\n" +
                "-- DROP FUNCTION public.make_transaction(bigint, bigint, money);\n" +
                "\n" +
                "CREATE OR REPLACE FUNCTION public.make_transaction(\n" +
                "\t\"from\" bigint,\n" +
                "\t\"to\" bigint,\n" +
                "\tamount money)\n" +
                "    RETURNS bigint\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "\n" +
                "    COST 100\n" +
                "    VOLATILE\n" +
                "AS $BODY$\n" +
                "declare\n" +
                "  tid bigint;\n" +
                "begin\n" +
                "  call update_transaction_method(\"from\",-1*amount);\n" +
                "  call update_transaction_method(\"to\",amount);\n" +
                "  insert into transactions(\"from\", \"to\", balance) values (\"from\",\"to\",amount) returning transaction_id into tid;\n" +
                "  return tid;\n" +
                "end;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.make_transaction(bigint, bigint, money)\n" +
                "    OWNER TO postgres;\n" +
                "===\n" +
                "select make_transaction(246,205,10::money);\n" +
                "select * from transactions;\n"));
        queries.add(new SQLQuery("Validate Login","-- FUNCTION: public.validate_login(character varying, text)\n" +
                "\n" +
                "-- DROP FUNCTION public.validate_login(character varying, text);\n" +
                "\n" +
                "CREATE OR REPLACE FUNCTION public.validate_login(\n" +
                "\tphone character varying,\n" +
                "\tpswd text)\n" +
                "    RETURNS boolean\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "\n" +
                "    COST 100\n" +
                "    VOLATILE\n" +
                "AS $BODY$declare\n" +
                "\thashpass text;\n" +
                "begin\n" +
                "\tselect \"password\"\n" +
                "\tinto hashpass\n" +
                "\tfrom nexusaccounts\n" +
                "\twhere phone_no=phone;\n" +
                "\tif hashpass is null then\n" +
                "\t\treturn false;\n" +
                "\tend if;\n" +
                "\treturn (crypt(pswd,hashpass))=hashpass;\n" +
                "\n" +
                "end;$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.validate_login(character varying, text)\n" +
                "    OWNER TO postgres;\n" +
                "===\n" +
                "select validate_login('01998561074','01998561074');"));
        return queries;
    }

    public static List<SQLQuery> getTriggers() {
        List<SQLQuery> triggers = new ArrayList<>();

        return triggers;
    }
}
