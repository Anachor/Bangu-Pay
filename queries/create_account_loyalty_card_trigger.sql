-- FUNCTION: public.create_loyalty_card_for_nexus_account()

-- DROP FUNCTION public.create_loyalty_card_for_nexus_account();

CREATE FUNCTION public.create_loyalty_card_for_nexus_account()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$DECLARE loyalty_id bigint;
BEGIN
  insert into loyalty_cards(balance) values(0)
  returning card_no into loyalty_id;
  insert into account_cards(phone_no,card_no) values(new.phone_no,loyalty_id);
  RETURN NEW;
END;$BODY$;

ALTER FUNCTION public.create_loyalty_card_for_nexus_account()
    OWNER TO postgres;

-- Trigger: create_first_loyalty_card_trigger

-- DROP TRIGGER create_first_loyalty_card_trigger ON public.nexusaccounts;

CREATE TRIGGER create_first_loyalty_card_trigger
    AFTER INSERT
    ON public.nexusaccounts
    FOR EACH ROW
    EXECUTE PROCEDURE public.create_loyalty_card_for_nexus_account();
===
call create_nexus_account('99999999998','Monkey','Monkey@gmail.com','Monkey');
select * from account_cards;