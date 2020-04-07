/********************
 *       Game       *
 ********************/

CREATE TABLE Game_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	LocalId INT UNIQUE NOT NULL,
	GameType VARCHAR(255) NOT NULL,
	GameParams VARCHAR(255) NOT NULL,
	DtFinish TIMESTAMP NOT NULL,
	DartsMatchId VARCHAR(36) NOT NULL,
	MatchOrdinal INT NOT NULL
);

INSERT INTO
	Game_Tmp
SELECT
	RowId,
	DtCreation,
	DtLastUpdate,
	LocalId,
	CASE
	    WHEN GameType = 1 THEN 'X01'
	    WHEN GameType = 2 THEN 'GOLF'
	    WHEN GameType = 3 THEN 'ROUND_THE_CLOCK'
	    ELSE 'DARTZEE'
	END,
	GameParams,
	DtFinish,
	DartsMatchId,
	MatchOrdinal
FROM
	Game;

RENAME TABLE Game TO zzGame;
RENAME TABLE Game_Tmp TO Game;
DROP TABLE zzGame;

CREATE INDEX GameType ON Game(GameType)