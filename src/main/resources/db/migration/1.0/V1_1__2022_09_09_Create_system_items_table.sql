create table system_items
(
    id        uuid primary key,
    parent_id uuid,
    size      bigint not null default 0,
    url       varchar(255),
    type      varchar(50) not null,
    date      timestamp not null
);