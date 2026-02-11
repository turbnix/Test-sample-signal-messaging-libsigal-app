--liquibase formatted sql

--precondition-name precondition-attribute:value

--changeset id:init-01 author:emil logicalFilePath:init.sql
create table devices
(
    device_id      int,
    service_id     varbinary(17),
    public_key     varbinary(64),
    signed_pre_key varbinary(255),
    CONSTRAINT pk_devices PRIMARY KEY (device_id, service_id)
)

--changeset id:init-02 author:emil logicalFilePath:init.sql
create table device_keys
(
    device_id  int,
    service_id varbinary(17),
    id         int,
    pre_key    varbinary(255),
    CONSTRAINT pk_device_keys PRIMARY KEY (device_id, service_id, id),
    CONSTRAINT fk_device_keys_devices FOREIGN KEY (device_id, service_id) REFERENCES devices (device_id, service_id)
)

--changeset id:init-03 author:emil logicalFilePath:init.sql
create table messages
(
    message_id          CHAR(36),
    sender_device_id    int,
    sender_service_id   varbinary(17),
    receiver_device_id  int,
    receiver_service_id varbinary(17),
    message             varbinary(255),
    message_type        int,
    CONSTRAINT pk_messages PRIMARY KEY (message_id),
    CONSTRAINT fk_sender_messages_devices FOREIGN KEY (sender_device_id, sender_service_id) REFERENCES devices (device_id, service_id),
    CONSTRAINT fk_receiver_messages_devices FOREIGN KEY (receiver_device_id, receiver_service_id) REFERENCES devices (device_id, service_id)
)

--changeset id:init-04 author:emil logicalFilePath:init.sql
create table users
(
    username   varchar(255),
    service_id varbinary(17),
    device_id  int,
    CONSTRAINT pk_users PRIMARY KEY (username)
)