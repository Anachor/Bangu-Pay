-- FUNCTION: public.get_transaction_method_id_of_card(bigint)

-- DROP FUNCTION public.get_transaction_method_id_of_card(bigint);

CREATE OR REPLACE FUNCTION public.get_transaction_method_id_of_card(
	card_no bigint)
    RETURNS bigint
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE
AS $BODY$declare
	tid bigint;
begin
	select transaction_method_id into tid
	from transaction_methods
	where type = 'card' and sub_class_primary_key = card_no;
	return tid;
end;$BODY$;

ALTER FUNCTION public.get_transaction_method_id_of_card(bigint)
    OWNER TO postgres;
===
select get_transaction_method_id_of_card(4611686018427388083);