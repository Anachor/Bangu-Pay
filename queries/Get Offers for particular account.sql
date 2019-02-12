select *
from offers
where
  card_no = 4611686018427388083 and (type = 'global'
  or 534 in
     (select transaction_method_id from local_offer_outlets loo where loo.local_offer_id = offer_id)
  );