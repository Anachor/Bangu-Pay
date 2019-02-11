select *
  from transactions
  where exists
    (select transaction_method_id
      from transaction_methods
      where sub_class_primary_key in (
        select card_no
        from account_cards
         where phone_no = '019215151'
        and transaction_method_id in ("from", "to")
      )
    );
