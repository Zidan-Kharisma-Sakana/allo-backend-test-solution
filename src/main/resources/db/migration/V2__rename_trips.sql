ALTER TABLE trips RENAME TO bill_groups;

ALTER TABLE participants RENAME COLUMN trip_id TO bill_group_id;

ALTER TABLE expenses RENAME COLUMN trip_id TO bill_group_id;
