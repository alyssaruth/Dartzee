ALTER TABLE Participant ADD COLUMN Resigned BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE Participant ALTER COLUMN Resigned DROP DEFAULT
