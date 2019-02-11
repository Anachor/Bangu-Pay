select n.phone_no, name, email_id
from account_cards
  join nexusaccounts n on account_cards.phone_no = n.phone_no
where card_no = '42'