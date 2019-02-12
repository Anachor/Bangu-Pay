-- PROCEDURE: public.create_nexus_account(character varying, text, text, text)

-- DROP PROCEDURE public.create_nexus_account(character varying, text, text, text);

CREATE OR REPLACE PROCEDURE public.create_nexus_account(
	phone_no character varying,
	name text,
	email text,
	password text)
LANGUAGE 'plpgsql'

AS $BODY$begin
	insert into nexusaccounts(phone_no,name,email_id,password)
	values(phone_no,name,email,crypt(password,gen_salt('bf')));
end;$BODY$;
===
call create_nexus_account('99999999998','Monkey','Monkey@gmail.com','Monkey');
select * from nexusaccounts;