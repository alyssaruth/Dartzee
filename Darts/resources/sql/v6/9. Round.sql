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
	zz.Guid,
	DtCreation,
	DtLastUpdate,
	zzP.Guid,
	RoundNumber
FROM
	Round r,
	zzRoundGuids zz,
	zzParticipantGuids zzP
WHERE
    r.RowId = zz.RowId
    AND r.ParticipantId = zzP.RowId;

RENAME TABLE Round TO zzRound;
RENAME TABLE Round_Tmp TO Round;
DROP TABLE zzRound