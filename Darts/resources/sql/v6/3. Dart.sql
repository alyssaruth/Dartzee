/********************
 *       Dart       *
 ********************/

CREATE TABLE Dart_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	RoundId VARCHAR(36) NOT NULL,
	Ordinal INT NOT NULL,
	Score INT NOT NULL,
	Multiplier INT NOT NULL,
	StartingScore INT NOT NULL,
	PosX INT NOT NULL,
	PosY INT NOT NULL,
	SegmentType INT NOT NULL
);

INSERT INTO
	Dart_Tmp
SELECT
	CAST(RowId AS CHAR(36)),
	DtCreation,
	DtLastUpdate,
	CAST(RoundId AS CHAR(36)),
	Ordinal,
	Score,
	Multiplier,
	StartingScore,
	PosX,
	PosY,
	SegmentType
FROM
	Dart;

RENAME TABLE Dart TO zzDart;
RENAME TABLE Dart_Tmp TO Dart;
DROP TABLE zzDart