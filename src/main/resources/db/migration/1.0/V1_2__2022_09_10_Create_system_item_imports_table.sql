create table system_item_imports
(
    id        bigint unsigned primary key auto_increment,
    parent_id varchar(255),
    child_id  varchar(255),
    depth     bigint unsigned default 0
);
