/********************
 *   Achievement    *
 ********************/

CREATE TABLE Achievement_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	PlayerId VARCHAR(36) NOT NULL,
	AchievementRef INT NOT NULL,
	GameIdEarned VARCHAR(36) NOT NULL,
	AchievementCounter INT NOT NULL,
	AchievementDetail VARCHAR(255) NOT NULL
);

INSERT INTO
	Achievement_Tmp
SELECT
	zz.Guid,
	DtCreation,
	DtLastUpdate,
	zzP.Guid,
	AchievementRef,
	CASE
	    WHEN zzG.Guid IS NULL THEN ''
	    ELSE zzG.Guid
	END,
	AchievementCounter,
	AchievementDetail
FROM
	Achievement a LEFT OUTER JOIN zzGameGuids zzG ON (a.GameIdEarned = zzG.RowId),
	zzPlayerGuids zzP,
	zzAchievementGuids zz
WHERE
    a.PlayerId = zzP.RowId
    AND a.RowId = zz.RowId;

RENAME TABLE Achievement TO zzAchievement;
RENAME TABLE Achievement_Tmp TO Achievement;
DROP TABLE zzAchievement;

CREATE INDEX PlayerId_AchievementRef ON Achievement(PlayerId, AchievementRef)