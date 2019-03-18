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
    CAST(RowId AS CHAR(36)),
    DtCreation,
    DtLastUpdate,
    Version
FROM
    Version;

RENAME TABLE Version TO zzVersion;
RENAME TABLE Version_Tmp TO Version;
DROP TABLE zzVersion;