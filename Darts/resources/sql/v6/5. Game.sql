/********************
 *       Game       *
 ********************/

CREATE TABLE Game_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	LocalId INT NOT NULL,
	GameType INT NOT NULL,
	GameParams VARCHAR(255) NOT NULL,
	DtFinish TIMESTAMP NOT NULL,
	DartsMatchId VARCHAR(36) NOT NULL,
	MatchOrdinal INT NOT NULL
);

INSERT INTO
	Game_Tmp
SELECT
	CAST(RowId AS CHAR(36)),
	DtCreation,
	DtLastUpdate,
	RowId,
	GameType,
	GameParams,
	DtFinish,
	CAST(DartsMatchId AS CHAR(36)),
	MatchOrdinal
FROM
	Game;

RENAME TABLE Game TO zzGame;
RENAME TABLE Game_Tmp TO Game;
DROP TABLE zzGame