CREATE TABLE users (
    ID UUID NOT NULL
        CONSTRAINT users_pk PRIMARY KEY,
    EMAIL varchar(150) NOT NULL,
    EMAIL_VERIFIED boolean NOT NULL DEFAULT FALSE,
    MOBILE_PHONE_NUMBER varchar(20) NOT NULL,
    MOBILE_PHONE_NUMBER_VERIFIED boolean NOT NULL DEFAULT FALSE,
    ACTIVE boolean NOT NULL DEFAULT FALSE,
    FIRST_NAME varchar(255) NOT NULL,
    LAST_NAME varchar(255) NOT NULL,
    REGISTERED_AT timestamp WITH TIME ZONE NOT NULL,
    TIME_ZONE_ID varchar(255) NOT NULL,
    CONSTRAINT user_email_uk UNIQUE (EMAIL),
    CONSTRAINT user_mobile_phone_number_uk UNIQUE (MOBILE_PHONE_NUMBER)
);

CREATE INDEX users_email_idx ON users (email);
CREATE INDEX users_mobile_phone_number_idx ON users (mobile_phone_number);

CREATE TABLE user_role (
    USER_ID UUID NOT NULL,
    ROLE varchar(20) NOT NULL,
    CONSTRAINT user_role_pk PRIMARY KEY (USER_ID, ROLE)
);

INSERT INTO users(ID,
                  EMAIL,
                  EMAIL_VERIFIED,
                  MOBILE_PHONE_NUMBER,
                  MOBILE_PHONE_NUMBER_VERIFIED,
                  ACTIVE,
                  FIRST_NAME,
                  LAST_NAME,
                  REGISTERED_AT,
                  TIME_ZONE_ID)
VALUES ('00000000-0000-0000-0000-000000000000',
        'admin@sbcgg.com',
        TRUE,
        '+5519999998888',
        TRUE,
        TRUE,
        'Admin',
        'Sbcgg',
        NOW(),
        'America/Sao_Paulo');

INSERT INTO user_role(USER_ID, ROLE)
VALUES ('00000000-0000-0000-0000-000000000000', 'ADMIN'),
       ('00000000-0000-0000-0000-000000000000', 'USER');