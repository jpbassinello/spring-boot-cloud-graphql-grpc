CREATE TABLE user_verification_code (
    ID UUID NOT NULL
        CONSTRAINT user_verification_code_pk PRIMARY KEY,
    USER_ID uuid NOT NULL,
    SOURCE varchar(150) NOT NULL, -- phone number, email address
    CODE varchar(6) NOT NULL,
    TYPE varchar(20) NOT NULL,
    VALID_UNTIL timestamp WITH TIME ZONE NOT NULL,
    CONFIRMED_AT timestamp WITH TIME ZONE
);
CREATE INDEX user_verification_code_user_id_source_idx ON user_verification_code (USER_ID, SOURCE);
