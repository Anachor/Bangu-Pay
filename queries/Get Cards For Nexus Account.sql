select cards.card_no, sub_class_type
from account_cards join cards on account_cards.card_no = cards.card_no
where phone_no = '01913373406';