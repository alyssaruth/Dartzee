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
	zz.Guid,
	DtCreation,
	DtLastUpdate,
	zzGame.Guid,
	zzPlayer.Guid,
	Ordinal,
	FinishingPosition,
	FinalScore,
	DtFinished
FROM
	Participant p,
	zzParticipantGuids zz,
	zzGameGuids zzGame,
	zzPlayerGuids zzPlayer
WHERE
    p.RowId = zz.RowId
    AND p.GameId = zzGame.RowId
    AND p.PlayerId = zzPlayer.RowId;

RENAME TABLE Participant TO zzParticipant;
RENAME TABLE Participant_Tmp TO Participant;
DROP TABLE zzParticipant;

CREATE INDEX PlayerId_GameId ON Participant(PlayerId, GameId)