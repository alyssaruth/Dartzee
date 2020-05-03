CREATE TABLE DartsMatch_Tmp
(
    RowId VARCHAR(36) PRIMARY KEY,
    DtCreation TIMESTAMP NOT NULL,
    DtLastUpdate TIMESTAMP NOT NULL,
    LocalId INT UNIQUE NOT NULL,
    Games INT NOT NULL,
    Mode VARCHAR(255) NOT NULL,
    DtFinish TIMESTAMP NOT NULL,
    MatchParams VARCHAR(500) NOT NULL
);

INSERT INTO
    DartsMatch_Tmp
SELECT
    RowId,
    DtCreation,
    DtLastUpdate,
    LocalId,
    Games,
    CASE
        WHEN Mode = 0 THEN 'FIRST_TO'
        ELSE 'POINTS'
    END,
    DtFinish,
    MatchParams
FROM
    DartsMatch;

RENAME TABLE DartsMatch TO zzDartsMatch;
RENAME TABLE DartsMatch_Tmp TO DartsMatch;
DROP TABLE zzDartsMatch