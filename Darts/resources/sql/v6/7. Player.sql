/********************
 *      Player      *
 ********************/

CREATE TABLE Player_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	Name varchar(25) NOT NULL,
	Strategy int NOT NULL,
	StrategyXml varchar(1000) NOT NULL,
	DtDeleted timestamp NOT NULL,
	PlayerImageId VARCHAR(36) NOT NULL
);

INSERT INTO
	Player_Tmp
SELECT
	zzP.Guid,
	DtCreation,
	DtLastUpdate,
	Name,
	Strategy,
	StrategyXml,
	DtDeleted,
	zzPi.Guid
FROM
	Player p,
	zzPlayerGuids zzP,
	zzPlayerImageGuids zzPi
WHERE
    p.RowId = zzP.RowId
    AND p.PlayerImageId = zzPi.RowId;

RENAME TABLE Player TO zzPlayer;
RENAME TABLE Player_Tmp TO Player;
DROP TABLE zzPlayer;

CREATE INDEX Name ON Player(Name);
CREATE INDEX Strategy_DtDeleted ON Player(Strategy, DtDeleted)