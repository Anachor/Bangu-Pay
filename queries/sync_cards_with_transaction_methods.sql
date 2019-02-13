-- FUNCTION: public.sync_cards_with_transaction_methods()

-- DROP FUNCTION public.sync_cards_with_transaction_methods();

CREATE FUNCTION public.sync_cards_with_transaction_methods()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$BEGIN
  insert into transaction_methods(sub_class_primary_key,type)
  values (NEW.card_no,'card');
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.sync_cards_with_transaction_methods()
    OWNER TO postgres;

-- Trigger: sync_cards_with_transaction_methods_trigger

-- DROP TRIGGER sync_cards_with_transaction_methods_trigger ON public.cards;

CREATE TRIGGER sync_cards_with_transaction_methods_trigger
    AFTER INSERT
    ON public.cards
    FOR EACH ROW
    EXECUTE PROCEDURE public.sync_cards_with_transaction_methods();
===
insert into debit_cards(account_no) values (300);
select * from transaction_methods;