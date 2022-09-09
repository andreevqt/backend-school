create table system_items
(
    id        uuid primary key,
    parent_id bigint,
    size      bigint,
    url       varchar(255),
    type      varchar(50)
);