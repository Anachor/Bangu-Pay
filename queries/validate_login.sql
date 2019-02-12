-- FUNCTION: public.validate_login(character varying, text)

-- DROP FUNCTION public.validate_login(character varying, text);

CREATE OR REPLACE FUNCTION public.validate_login(
	phone character varying,
	pswd text)
    RETURNS boolean
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE
AS $BODY$declare
	hashpass text;
begin
	select "password"
	into hashpass
	from nexusaccounts
	where phone_no=phone;
	if hashpass is null then
		return false;
	end if;
	return (crypt(pswd,hashpass))=hashpass;

end;$BODY$;

ALTER FUNCTION public.validate_login(character varying, text)
    OWNER TO postgres;
===
select validate_login('01998561074','01998561074');