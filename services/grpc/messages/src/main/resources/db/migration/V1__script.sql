CREATE TABLE messages (
    ID UUID NOT NULL
        CONSTRAINT messages_pk PRIMARY KEY,
    USER_ID UUID NOT NULL,
    CHANNEL varchar(20) NOT NULL,
    TEMPLATE varchar(30) NOT NULL,
    RECIPIENT varchar(150) NOT NULL,
    IDEMPOTENCE_KEY varchar(100),
    REGISTERED_AT timestamp WITH TIME ZONE NOT NULL,
    SCHEDULED_AT timestamp WITH TIME ZONE NOT NULL,
    SENT_AT timestamp WITH TIME ZONE,
    NEXT_ATTEMPT_AT timestamp WITH TIME ZONE NOT NULL,
    RETRIES int NOT NULL DEFAULT 0,
    STATUS varchar(30) NOT NULL,
    CONSTRAINT messages_uk UNIQUE (IDEMPOTENCE_KEY, CHANNEL)
);
CREATE INDEX messages_user_id_idx ON messages (USER_ID);
CREATE INDEX messages_status_idx ON messages (STATUS);

CREATE TABLE message_variable (
    MESSAGE_ID UUID NOT NULL,
    KEY varchar(100) NOT NULL,
    VALUE varchar(1000) NOT NULL,
    CONSTRAINT message_variable_pk PRIMARY KEY (MESSAGE_ID, KEY)
);
