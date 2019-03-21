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
	zzG.Guid,
	DtCreation,
	DtLastUpdate,
	g.RowId,
	GameType,
	GameParams,
	DtFinish,
	CASE
	    WHEN zzM.Guid IS NULL THEN ''
	    ELSE zzM.Guid
	END,
	MatchOrdinal
FROM
	Game g LEFT OUTER JOIN zzDartsMatchGuids zzM ON (g.DartsMatchId = zzM.RowId),
	zzGameGuids zzG
WHERE
    g.RowId = zzG.RowId;

RENAME TABLE Game TO zzGame;
RENAME TABLE Game_Tmp TO Game;
DROP TABLE zzGame;

CREATE INDEX GameType ON Game(GameType)