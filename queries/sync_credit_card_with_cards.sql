-- FUNCTION: public.sync_credit_card_with_cards()

-- DROP FUNCTION public.sync_credit_card_with_cards();

CREATE FUNCTION public.sync_credit_card_with_cards()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$BEGIN
  insert into cards(card_no,sub_class_type)
  values (NEW.card_no,'credit_card');
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.sync_credit_card_with_cards()
    OWNER TO postgres;
===
insert into credit_cards(monthly_limit,daily_limit,interest_rate,deadline,owner) values (1000000::money,1000000::money,0.5,current_date + 7, 245);
select * from cards;