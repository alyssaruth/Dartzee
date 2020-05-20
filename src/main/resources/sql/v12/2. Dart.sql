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
    SegmentType VARCHAR(255) NOT NULL
);

INSERT INTO
    Dart_Tmp
SELECT
    RowId,
    DtCreation,
    DtLastUpdate,
    PlayerId,
    ParticipantId,
    RoundNumber,
    Ordinal,
    Score,
    Multiplier,
    StartingScore,
    PosX,
    PosY,
    CASE
        WHEN SegmentType = 1 THEN 'DOUBLE'
        WHEN SegmentType = 2 THEN 'TREBLE'
        WHEN SegmentType = 3 THEN 'OUTER_SINGLE'
        WHEN SegmentType = 4 THEN 'INNER_SINGLE'
        WHEN SegmentType = 5 THEN 'MISS'
        ELSE 'MISSED_BOARD'
    END
FROM
    Dart;

RENAME TABLE Dart TO zzDart;
RENAME TABLE Dart_Tmp TO Dart;
DROP TABLE zzDart