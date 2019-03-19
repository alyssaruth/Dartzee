/********************
 *    DartsMatch    *
 ********************/

CREATE TABLE DartsMatch_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	LocalId INT NOT NULL,
	Games INT NOT NULL,
	Mode INT NOT NULL,
	DtFinish TIMESTAMP NOT NULL,
	MatchParams VARCHAR(255) NOT NULL
);

INSERT INTO
	DartsMatch_Tmp
SELECT
	CAST(RowId AS CHAR(36)),
	DtCreation,
	DtLastUpdate,
	RowId,
	Games,
	Mode,
	DtFinish,
	MatchParams
FROM
	DartsMatch;

RENAME TABLE DartsMatch TO zzDartsMatch;
RENAME TABLE DartsMatch_Tmp TO DartsMatch;
DROP TABLE zzDartsMatch