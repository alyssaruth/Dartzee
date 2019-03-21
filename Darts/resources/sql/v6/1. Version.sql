/********************
 *     Version      *
 ********************/

CREATE TABLE Version_Tmp
(
    RowId VARCHAR(36) PRIMARY KEY,
    DtCreation TIMESTAMP NOT NULL,
    DtLastUpdate TIMESTAMP NOT NULL,
    Version INT NOT NULL
);

INSERT INTO
    Version_Tmp
SELECT
    zz.Guid,
    DtCreation,
    DtLastUpdate,
    Version
FROM
    Version v,
    zzVersionGuids zz
WHERE
    v.RowId = zz.RowId;


RENAME TABLE Version TO zzVersion;
RENAME TABLE Version_Tmp TO Version;
DROP TABLE zzVersion