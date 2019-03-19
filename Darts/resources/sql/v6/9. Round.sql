/********************
 *      Round       *
 ********************/

CREATE TABLE Round_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	ParticipantId VARCHAR(36) NOT NULL,
	RoundNumber INT NOT NULL
);

INSERT INTO
	Round_Tmp
SELECT
	CAST(RowId AS CHAR(36)),
	DtCreation,
	DtLastUpdate,
	CAST(ParticipantId AS CHAR(36)),
	RoundNumber
FROM
	Round;

RENAME TABLE Round TO zzRound;
RENAME TABLE Round_Tmp TO Round;
DROP TABLE zzRound