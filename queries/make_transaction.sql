-- FUNCTION: public.make_transaction(bigint, bigint, money)

-- DROP FUNCTION public.make_transaction(bigint, bigint, money);

CREATE OR REPLACE FUNCTION public.make_transaction(
	"from" bigint,
	"to" bigint,
	amount money)
    RETURNS bigint
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE
AS $BODY$
declare
  tid bigint;
begin
  call update_transaction_method("from",-1*amount);
  call update_transaction_method("to",amount);
  insert into transactions("from", "to", balance) values ("from","to",amount) returning transaction_id into tid;
  return tid;
end;
$BODY$;

ALTER FUNCTION public.make_transaction(bigint, bigint, money)
    OWNER TO postgres;
===
select make_transaction(246,205,10::money);
select * from transactions;
