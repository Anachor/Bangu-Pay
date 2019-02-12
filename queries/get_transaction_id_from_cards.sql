-- FUNCTION: public.get_transaction_method_id_of_account(bigint)

-- DROP FUNCTION public.get_transaction_method_id_of_account(bigint);

CREATE OR REPLACE FUNCTION public.get_transaction_method_id_of_account(
	account_no bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE
AS $BODY$declare
	tid bigint;
begin
	select transaction_method_id into tid
	from transaction_methods
	where type = 'account' and sub_class_primary_key = account_no;
	return tid;
end;$BODY$;

ALTER FUNCTION public.get_transaction_method_id_of_account(bigint)
    OWNER TO postgres;
===
