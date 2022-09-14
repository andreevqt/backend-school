create table system_items
(
    id        varchar(255) primary key,
    parent_id varchar(255),
    size      bigint unsigned default 0,
    url       varchar(255),
    type      varchar(50)     not null,
    date      timestamp       not null
);
