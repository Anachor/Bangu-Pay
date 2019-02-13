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
        queries.add(new SQLQuery("Get Nexus Account For Phone No",
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

        queries.add(new SQLQuery("Show Transaction Method ID of Account","-- FUNCTION: public.get_transaction_method_id_of_account(bigint)\n" +
                "\n" +
                "-- DROP FUNCTION public.get_transaction_method_id_of_account(bigint);\n" +
                "\n" +
                "CREATE OR REPLACE FUNCTION public.get_transaction_method_id_of_account(\n" +
                "\taccount_no bigint)\n" +
                "    RETURNS bigint\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "\n" +
                "    COST 100\n" +
                "    VOLATILE\n" +
                "AS $BODY$declare\n" +
                "\ttid bigint;\n" +
                "begin\n" +
                "\tselect transaction_method_id into tid\n" +
                "\tfrom transaction_methods\n" +
                "\twhere type = 'account' and sub_class_primary_key = account_no;\n" +
                "\treturn tid;\n" +
                "end;$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.get_transaction_method_id_of_account(bigint)\n" +
                "    OWNER TO postgres;\n" +
                "===\n" +
                "select get_transaction_method_id_of_account(315);"));

        queries.add(new SQLQuery("Show Transaction Method ID of Card","-- FUNCTION: public.get_transaction_method_id_of_card(bigint)\n" +
                "\n" +
                "-- DROP FUNCTION public.get_transaction_method_id_of_card(bigint);\n" +
                "\n" +
                "CREATE OR REPLACE FUNCTION public.get_transaction_method_id_of_card(\n" +
                "\tcard_no bigint)\n" +
                "    RETURNS bigint\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "\n" +
                "    COST 100\n" +
                "    VOLATILE\n" +
                "AS $BODY$declare\n" +
                "\ttid bigint;\n" +
                "begin\n" +
                "\tselect transaction_method_id into tid\n" +
                "\tfrom transaction_methods\n" +
                "\twhere type = 'card' and sub_class_primary_key = card_no;\n" +
                "\treturn tid;\n" +
                "end;$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.get_transaction_method_id_of_card(bigint)\n" +
                "    OWNER TO postgres;\n" +
                "===\n" +
                "select get_transaction_method_id_of_card(4611686018427388083);"));
        return queries;
    }

    public static List<SQLQuery> getTriggers() {
        List<SQLQuery> triggers = new ArrayList<>();

        triggers.add(new SQLQuery("Create Nexus Account Trigger","-- FUNCTION: public.create_loyalty_card_for_nexus_account()\n" +
                "\n" +
                "-- DROP FUNCTION public.create_loyalty_card_for_nexus_account();\n" +
                "\n" +
                "CREATE FUNCTION public.create_loyalty_card_for_nexus_account()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$DECLARE loyalty_id bigint;\n" +
                "BEGIN\n" +
                "  insert into loyalty_cards(balance) values(0)\n" +
                "  returning card_no into loyalty_id;\n" +
                "  insert into account_cards(phone_no,card_no) values(new.phone_no,loyalty_id);\n" +
                "  RETURN NEW;\n" +
                "END;$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.create_loyalty_card_for_nexus_account()\n" +
                "    OWNER TO postgres;\n" +
                "\n" +
                "-- Trigger: create_first_loyalty_card_trigger\n" +
                "\n" +
                "-- DROP TRIGGER create_first_loyalty_card_trigger ON public.nexusaccounts;\n" +
                "\n" +
                "CREATE TRIGGER create_first_loyalty_card_trigger\n" +
                "    AFTER INSERT\n" +
                "    ON public.nexusaccounts\n" +
                "    FOR EACH ROW\n" +
                "    EXECUTE PROCEDURE public.create_loyalty_card_for_nexus_account();\n" +
                "===\n" +
                "call create_nexus_account('99999999998','Monkey','Monkey@gmail.com','Monkey');\n" +
                "select * from account_cards;"));

        triggers.add(new SQLQuery("Credit Card Sync With Cards","-- FUNCTION: public.sync_credit_card_with_cards()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_credit_card_with_cards();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_credit_card_with_cards()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into cards(card_no,sub_class_type)\n" +
                "  values (NEW.card_no,'credit_card');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_credit_card_with_cards()\n" +
                "    OWNER TO postgres;\n" +
                "===\n" +
                "insert into credit_cards(monthly_limit,daily_limit,interest_rate,deadline,owner) values (1000000::money,1000000::money,0.5,current_date + 7, 245);\n" +
                "select * from cards;"));
        triggers.add(new SQLQuery("Debit Cards Sync With Cards","-- FUNCTION: public.sync_debit_cards_with_cards()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_debit_cards_with_cards();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_debit_cards_with_cards()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into cards(card_no,sub_class_type)\n" +
                "  values (NEW.card_no,'debit_card');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_debit_cards_with_cards()\n" +
                "    OWNER TO postgres;\n" +
                "===\n" +
                "insert into debit_cards(account_no) values (300);\n" +
                "select * from cards;"));
        triggers.add(new SQLQuery("Sync Loyalty Cards with Cards","-- FUNCTION: public.sync_loyalty_cards_with_cards()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_loyalty_cards_with_cards();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_loyalty_cards_with_cards()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into cards(card_no,sub_class_type)\n" +
                "  values (NEW.card_no,'loyalty_card');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_loyalty_cards_with_cards()\n" +
                "    OWNER TO postgres;\n" +
                "\n" +
                "-- Trigger: sync_loyalty_card_with_cards_trigger\n" +
                "\n" +
                "-- DROP TRIGGER sync_loyalty_card_with_cards_trigger ON public.loyalty_cards;\n" +
                "\n" +
                "CREATE TRIGGER sync_loyalty_card_with_cards_trigger\n" +
                "    AFTER INSERT\n" +
                "    ON public.loyalty_cards\n" +
                "    FOR EACH ROW\n" +
                "    EXECUTE PROCEDURE public.sync_loyalty_cards_with_cards();\n" +
                "\n" +
                "===\n" +
                "call create_nexus_account('99999999998','Monkey','Monkey@gmail.com','Monkey');\n" +
                "select * from cards;"));

        triggers.add(new SQLQuery("Sync Rocket With Cards","-- FUNCTION: public.sync_rocket_with_cards()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_rocket_with_cards();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_rocket_with_cards()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into cards(card_no,sub_class_type)\n" +
                "  values (NEW.card_no,'rocket_card');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_rocket_with_cards()\n" +
                "    OWNER TO postgres;\n" +
                "===\n" +
                "insert into rocket_cards(phone_no) values('99999999999');\n" +
                "select * from cards;"));

        triggers.add(new SQLQuery("Sync Cards with Transaction Methods","-- FUNCTION: public.sync_cards_with_transaction_methods()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_cards_with_transaction_methods();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_cards_with_transaction_methods()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into transaction_methods(sub_class_primary_key,type)\n" +
                "  values (NEW.card_no,'card');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_cards_with_transaction_methods()\n" +
                "    OWNER TO postgres;\n" +
                "\n" +
                "-- Trigger: sync_cards_with_transaction_methods_trigger\n" +
                "\n" +
                "-- DROP TRIGGER sync_cards_with_transaction_methods_trigger ON public.cards;\n" +
                "\n" +
                "CREATE TRIGGER sync_cards_with_transaction_methods_trigger\n" +
                "    AFTER INSERT\n" +
                "    ON public.cards\n" +
                "    FOR EACH ROW\n" +
                "    EXECUTE PROCEDURE public.sync_cards_with_transaction_methods();\n" +
                "===\n" +
                "insert into debit_cards(account_no) values (300);\n" +
                "select * from transaction_methods;"));

        triggers.add(new SQLQuery("Sync Accounts With Transaction Methods","-- FUNCTION: public.sync_accounts_with_transaction_method()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_accounts_with_transaction_method();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_accounts_with_transaction_method()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into transaction_methods(sub_class_primary_key,type)\n" +
                "  values (NEW.account_no,'account');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_accounts_with_transaction_method()\n" +
                "    OWNER TO postgres;\n" +
                "\n" +
                "-- Trigger: sync_accounts_with_transaction_methods_trigger\n" +
                "\n" +
                "-- DROP TRIGGER sync_accounts_with_transaction_methods_trigger ON public.accounts;\n" +
                "\n" +
                "CREATE TRIGGER sync_accounts_with_transaction_methods_trigger\n" +
                "    AFTER INSERT\n" +
                "    ON public.accounts\n" +
                "    FOR EACH ROW\n" +
                "    EXECUTE PROCEDURE public.sync_accounts_with_transaction_method();\n" +
                "===\n" +
                "insert into accounts(owner) values(245);\n" +
                "select * from transaction_methods;"));

        triggers.add(new SQLQuery("Sync Persons with Financial Entities","-- FUNCTION: public.sync_persons_with_financial_entities()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_persons_with_financial_entities();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_persons_with_financial_entities()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into financial_entities(sub_class_primary_key,type)\n" +
                "  values (NEW.nid,'person');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_persons_with_financial_entities()\n" +
                "    OWNER TO postgres;\n" +
                "-- Trigger: sync_persons_with_financial_entities_trigger\n" +
                "\n" +
                "-- DROP TRIGGER sync_persons_with_financial_entities_trigger ON public.persons;\n" +
                "\n" +
                "CREATE TRIGGER sync_persons_with_financial_entities_trigger\n" +
                "    AFTER INSERT\n" +
                "    ON public.persons\n" +
                "    FOR EACH ROW\n" +
                "    EXECUTE PROCEDURE public.sync_persons_with_financial_entities();\n" +
                "===\n" +
                "insert INTO persons(phone_no,nid,name) values('99999999999',705,'A');\n" +
                "select * from financial_entities;"));

        triggers.add(new SQLQuery("Sync Corporations with Financial Entities","-- FUNCTION: public.sync_corporations_with_financial_entities()\n" +
                "\n" +
                "-- DROP FUNCTION public.sync_corporations_with_financial_entities();\n" +
                "\n" +
                "CREATE FUNCTION public.sync_corporations_with_financial_entities()\n" +
                "    RETURNS trigger\n" +
                "    LANGUAGE 'plpgsql'\n" +
                "    COST 100\n" +
                "    VOLATILE NOT LEAKPROOF\n" +
                "AS $BODY$BEGIN\n" +
                "  insert into financial_entities(sub_class_primary_key,type)\n" +
                "  values (NEW.registration_no,'corporation');\n" +
                "  RETURN NEW;\n" +
                "END;\n" +
                "$BODY$;\n" +
                "\n" +
                "ALTER FUNCTION public.sync_corporations_with_financial_entities()\n" +
                "    OWNER TO postgres;\n" +
                "-- Trigger: sync_corporations_with_financial_entities_trigger\n" +
                "\n" +
                "-- DROP TRIGGER sync_corporations_with_financial_entities_trigger ON public.corporations;\n" +
                "\n" +
                "CREATE TRIGGER sync_corporations_with_financial_entities_trigger\n" +
                "    AFTER INSERT\n" +
                "    ON public.corporations\n" +
                "    FOR EACH ROW\n" +
                "    EXECUTE PROCEDURE public.sync_corporations_with_financial_entities();\n" +
                "===\n" +
                "insert INTO corporations(phone_no,name,registration_no) values('99999999999','AAA',10111110001);\n" +
                "select * from financial_entities;"));

        return triggers;
    }
}
