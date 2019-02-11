select *
  from transactions
  where
    (select transaction_method_id
      from transaction_methods
      where sub_class_primary_key = '61'
    ) in ("from", "to");
