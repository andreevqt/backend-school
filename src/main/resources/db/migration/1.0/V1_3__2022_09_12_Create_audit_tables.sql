create table revinfo
(
    rev      int(11)    not null auto_increment,
    revtstmp bigint(20) null default null,
    primary key (rev)
);

create table system_item_system_item_aud
(
    rev       int(11)      not null,
    parent_id varchar(255) not null,
    id        varchar(255) not null,
    revtype   tinyint(4)   null default null,
    primary key (rev, parent_id, id),
    foreign key (rev) references revinfo (rev) on update restrict on delete restrict
);

create table system_items_aud
(
    id            varchar(255) not null,
    rev           int(11)      not null,
    revtype       tinyint(4)   null default null,
    date          datetime     null default null,
    date_mod      bit(1)       null default null,
    parent_id     varchar(255) null default null,
    parent_id_mod bit(1)       null default null,
    size          bigint(20)   unsigned null default null,
    size_mod      bit(1)       null default null,
    type          varchar(255) null default null,
    type_mod      bit(1)       null default null,
    url           varchar(255) null default null,
    url_mod       bit(1)       null default null,
    primary key (id, rev),
    foreign key (rev) references revinfo (rev) on update restrict on delete restrict
);
