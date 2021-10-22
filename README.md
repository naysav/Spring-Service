# Spring-Service - http://87.247.157.161:8080/Service/

Используемые таблицы:

```sql
create table customer
(
    id              bigint       not null
        primary key,
    age             varchar(255) null,
    first_name      varchar(255) null,
    gender          varchar(255) null,
    last_name       varchar(255) null,
    link_to_file    varchar(255) null,
    passport_number varchar(255) null,
    passport_series varchar(255) null,
    phone_number    varchar(255) null
);
```

```sql
create table user
(
    id              bigint       not null
        primary key,
    age             varchar(255) null,
    first_name      varchar(255) null,
    gender          varchar(255) null,
    last_name       varchar(255) null,
    password        varchar(255) null,
    password_verify varchar(255) null,
    role            varchar(255) null,
    username        varchar(12)  null
):
```

```sql
create table hibernate_sequence
(
    next_val bigint null
);
```
