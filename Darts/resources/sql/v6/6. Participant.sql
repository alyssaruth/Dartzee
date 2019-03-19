/********************
 *   Participant    *
 ********************/

CREATE TABLE Participant_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	GameId VARCHAR(36) NOT NULL,
	PlayerId VARCHAR(36) NOT NULL,
	Ordinal INT NOT NULL,
	FinishingPosition INT NOT NULL,
	FinalScore INT NOT NULL,
	DtFinished TIMESTAMP NOT NULL
);

INSERT INTO
	Participant_Tmp
SELECT
	CAST(RowId AS CHAR(36)),
	DtCreation,
	DtLastUpdate,
	CAST(GameId AS CHAR(36)),
	CAST(PlayerId AS CHAR(36)),
	Ordinal,
	FinishingPosition,
	FinalScore,
	DtFinished
FROM
	Participant;

RENAME TABLE Participant TO zzParticipant;
RENAME TABLE Participant_Tmp TO Participant;
DROP TABLE zzParticipant