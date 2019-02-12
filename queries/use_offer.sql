-- PROCEDURE: public.use_offer(bigint, bigint)

-- DROP PROCEDURE public.use_offer(bigint, bigint);

CREATE OR REPLACE PROCEDURE public.use_offer(
	offer_no bigint,
	transaction_no bigint)
LANGUAGE 'plpgsql'

AS $BODY$
declare
  cash_back                 money;
  max_amount                money;
  used                      money;
  transaction_money         money;
  source                    bigint;
  destination               bigint;
  rate                      numeric;
begin
  select cashback_percentage,"limit",used_so_far into rate,max_amount,used
  from offers where offer_id = offer_no;

  select balance,"from","to" into transaction_money,destination,source
  from transactions where  transaction_id = transaction_no;

  cash_back = least(max_amount-used,(transaction_money::numeric*rate)::money);

  perform make_transaction(source,destination,cash_back);

  update  offers set used_so_far = used_so_far + cash_back where offer_id=offer_no;

end;
$BODY$;
===
call use_offer(7,5);
select * from transactions;