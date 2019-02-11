select * from offers join card_offers co on offers.offer_id = co.offer_no
where card_no = ?