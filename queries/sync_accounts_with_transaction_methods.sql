-- FUNCTION: public.sync_accounts_with_transaction_method()

-- DROP FUNCTION public.sync_accounts_with_transaction_method();

CREATE FUNCTION public.sync_accounts_with_transaction_method()
    RETURNS trigger
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE NOT LEAKPROOF
AS $BODY$BEGIN
  insert into transaction_methods(sub_class_primary_key,type)
  values (NEW.account_no,'account');
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION public.sync_accounts_with_transaction_method()
    OWNER TO postgres;

-- Trigger: sync_accounts_with_transaction_methods_trigger

-- DROP TRIGGER sync_accounts_with_transaction_methods_trigger ON public.accounts;

CREATE TRIGGER sync_accounts_with_transaction_methods_trigger
    AFTER INSERT
    ON public.accounts
    FOR EACH ROW
    EXECUTE PROCEDURE public.sync_accounts_with_transaction_method();
===
insert into accounts(owner) values(245);
select * from transaction_methods;