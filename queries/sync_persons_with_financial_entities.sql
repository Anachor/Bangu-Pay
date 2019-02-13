-- FUNCTION: public.sync_persons_with_financial_entities()

-- DROP FUNCTION public.sync_persons_with_financial_entities();

CREATE FUNCTION public.sync_persons_with_financial_entities()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$BEGIN
  insert into financial_entities(sub_class_primary_key,type)
  values (NEW.nid,'person');
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.sync_persons_with_financial_entities()
    OWNER TO postgres;
-- Trigger: sync_persons_with_financial_entities_trigger

-- DROP TRIGGER sync_persons_with_financial_entities_trigger ON public.persons;

CREATE TRIGGER sync_persons_with_financial_entities_trigger
    AFTER INSERT
    ON public.persons
    FOR EACH ROW
    EXECUTE PROCEDURE public.sync_persons_with_financial_entities();
===
insert INTO persons(phone_no,nid,name) values('99999999999','705','A');
select * from financial_entities;