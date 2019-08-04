CREATE TABLE Dart_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	PlayerId VARCHAR(36) NOT NULL,
	ParticipantId VARCHAR(36) NOT NULL,
	RoundNumber INT NOT NULL,
	Ordinal INT NOT NULL,
	Score INT NOT NULL,
	Multiplier INT NOT NULL,
	StartingScore INT NOT NULL,
	PosX INT NOT NULL,
	PosY INT NOT NULL,
	SegmentType INT NOT NULL
);

INSERT INTO Dart_Tmp
SELECT
    drt.RowId,
    drt.DtCreation,
    drt.DtLastUpdate,
    pt.PlayerId,
    rnd.ParticipantId,
    rnd.RoundNumber,
    drt.Ordinal,
    drt.Score,
    drt.Multiplier,
    drt.StartingScore,
    drt.PosX,
    drt.PosY,
    drt.SegmentType
FROM
    Dart drt,
    Round rnd,
    Participant pt
WHERE
    drt.RoundId = rnd.RowId
    AND rnd.ParticipantId = pt.RowId;

RENAME TABLE Dart TO zzDart;
RENAME TABLE Dart_Tmp TO Dart;
DROP TABLE zzDart;

CREATE INDEX PlayerId_ParticipantId_RoundNumber_Ordinal ON Dart(PlayerId, ParticipantId, RoundNumber, Ordinal)