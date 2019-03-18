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
	CAST(RowId AS CHAR(36)),
	DtCreation,
	DtLastUpdate,
	CAST(PlayerId AS CHAR(36)),
	AchievementRef,
	CAST(GameIdEarned AS CHAR(36)),
	AchievementCounter,
	AchievementDetail
FROM
	Achievement;

RENAME TABLE Achievement TO zzAchievement;
RENAME TABLE Achievement_Tmp TO Achievement;
DROP TABLE zzAchievement