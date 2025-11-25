-- full text search column on users
CREATE EXTENSION unaccent;
ALTER TABLE users
    ADD COLUMN TERMS tsvector;
CREATE INDEX users_terms_idx ON users USING GIN (terms);
CREATE FUNCTION users_terms_update() RETURNS trigger AS
$$
BEGIN
    new.terms := TO_TSVECTOR(LOWER(new.EMAIL) || ' ' || COALESCE(new.MOBILE_PHONE_NUMBER, '') || ' ' ||
                             COALESCE(LOWER(unaccent(new.FIRST_NAME)), '') || ' ' ||
                             COALESCE(LOWER(unaccent(new.LAST_NAME)), ''));
    RETURN new;
END
$$ LANGUAGE plpgsql;
CREATE TRIGGER users_terms_update_trigger
    BEFORE INSERT OR UPDATE
    ON users
    FOR EACH ROW
EXECUTE FUNCTION users_terms_update();