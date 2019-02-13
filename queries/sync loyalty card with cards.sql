-- FUNCTION: public.sync_loyalty_cards_with_cards()

-- DROP FUNCTION public.sync_loyalty_cards_with_cards();

CREATE FUNCTION public.sync_loyalty_cards_with_cards()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$BEGIN
  insert into cards(card_no,sub_class_type)
  values (NEW.card_no,'loyalty_card');
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.sync_loyalty_cards_with_cards()
    OWNER TO postgres;

-- Trigger: sync_loyalty_card_with_cards_trigger

-- DROP TRIGGER sync_loyalty_card_with_cards_trigger ON public.loyalty_cards;

CREATE TRIGGER sync_loyalty_card_with_cards_trigger
    AFTER INSERT
    ON public.loyalty_cards
    FOR EACH ROW
    EXECUTE PROCEDURE public.sync_loyalty_cards_with_cards();

===
call create_nexus_account('99999999998','Monkey','Monkey@gmail.com','Monkey');
select * from cards;