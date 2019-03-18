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
	CAST(RowId AS CHAR(36)),
	DtCreation,
	DtLastUpdate,
	Name,
	Strategy,
	StrategyXml,
	DtDeleted,
	CAST(PlayerImageId AS CHAR(36))
FROM
	Player;

RENAME TABLE Player TO zzPlayer;
RENAME TABLE Player_Tmp TO Player;
DROP TABLE zzPlayer;