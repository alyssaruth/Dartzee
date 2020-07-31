ALTER TABLE Player DROP COLUMN Strategy;

ALTER TABLE Player RENAME COLUMN Strategy TO Strategy;

CREATE INDEX Strategy_DtDeleted ON Player(Strategy, DtDeleted)