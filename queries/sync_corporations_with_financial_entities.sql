-- FUNCTION: public.sync_corporations_with_financial_entities()

-- DROP FUNCTION public.sync_corporations_with_financial_entities();

CREATE FUNCTION public.sync_corporations_with_financial_entities()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$BEGIN
  insert into financial_entities(sub_class_primary_key,type)
  values (NEW.registration_no,'corporation');
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.sync_corporations_with_financial_entities()
    OWNER TO postgres;
-- Trigger: sync_corporations_with_financial_entities_trigger

-- DROP TRIGGER sync_corporations_with_financial_entities_trigger ON public.corporations;

CREATE TRIGGER sync_corporations_with_financial_entities_trigger
    AFTER INSERT
    ON public.corporations
    FOR EACH ROW
    EXECUTE PROCEDURE public.sync_corporations_with_financial_entities();
===
insert INTO corporations(phone_no,name,registration_no) values('99999999999','AAA',10111110001);
select * from financial_entities;