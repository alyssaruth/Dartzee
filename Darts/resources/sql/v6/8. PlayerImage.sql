/********************
 *   PlayerImage    *
 ********************/

CREATE TABLE PlayerImage_Tmp
(
	RowId VARCHAR(36) PRIMARY KEY,
	DtCreation TIMESTAMP NOT NULL,
	DtLastUpdate TIMESTAMP NOT NULL,
	BlobData Blob NOT NULL,
	Filepath VARCHAR(1000) NOT NULL,
	Preset BOOLEAN NOT NULL
);

INSERT INTO
	PlayerImage_Tmp
SELECT
	zz.Guid,
	DtCreation,
	DtLastUpdate,
	BlobData,
	Filepath,
	Preset
FROM
	PlayerImage pi,
	zzPlayerImageGuids zz
WHERE
    pi.RowId = zz.RowId;

RENAME TABLE PlayerImage TO zzPlayerImage;
RENAME TABLE PlayerImage_Tmp TO PlayerImage;
DROP TABLE zzPlayerImage