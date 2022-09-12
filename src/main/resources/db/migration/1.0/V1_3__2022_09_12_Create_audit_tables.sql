create table revinfo
(
    rev      bigserial primary key,
    revtstmp bigint
);

create table system_item_system_item_aud
(
    rev       bigint       not null,
    parent_id varchar(255) not null,
    id        varchar(255) not null,
    revtype   smallint,
    primary key (rev, parent_id, id),
    foreign key (rev) references revinfo
);

create table system_items_aud
(
    id            varchar(255) not null,
    rev           bigint       not null,
    revtype       smallint,
    date          timestamp,
    date_mod      boolean,
    parent_id     varchar(255),
    parent_id_mod boolean,
    size          bigint,
    size_mod      boolean,
    type          varchar(255),
    type_mod      boolean,
    url           varchar(255),
    url_mod       boolean,
    primary key (id, rev),
    foreign key (rev) references revinfo

);

