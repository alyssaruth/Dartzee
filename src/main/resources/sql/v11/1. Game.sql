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
	RowId,
	CASE
	    WHEN GameType IS 0 THEN 'X01'
	    WHEN GameType IS 1 THEN 'GOLF'
	    WHEN GameType IS 2 THEN 'ROUND_THE_CLOCK'
	    ELSE 'DARTZEE'
	END,
	GameParams,
	DtFinish,
	DartsMatchId,
	MatchOrdinal
FROM
	Game

RENAME TABLE Game TO zzGame;
RENAME TABLE Game_Tmp TO Game;
DROP TABLE zzGame;

CREATE INDEX GameType ON Game(GameType)