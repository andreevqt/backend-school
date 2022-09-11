create table system_item_imports
(
    id        bigserial primary key,
    parent_id uuid,
    child_id  uuid,
    depth     bigint default 0
);