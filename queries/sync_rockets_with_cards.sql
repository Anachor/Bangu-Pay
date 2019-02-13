-- FUNCTION: public.sync_rocket_with_cards()

-- DROP FUNCTION public.sync_rocket_with_cards();

CREATE FUNCTION public.sync_rocket_with_cards()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$BEGIN
  insert into cards(card_no,sub_class_type)
  values (NEW.card_no,'rocket_card');
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.sync_rocket_with_cards()
    OWNER TO postgres;
===
insert into rocket_cards(phone_no) values('99999999999');
select * from cards;