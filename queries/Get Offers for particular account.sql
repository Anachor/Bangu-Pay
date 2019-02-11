select *
from offers join card_offers co on offers.offer_id = co.offer_no
where
  card_no = '37' and (type = 'global'
  or '41' in
     (select transaction_method_id from local_offer_outlets loo where loo.local_offer_id = co.offer_no)
  )