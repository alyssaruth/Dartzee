package dartzee.sync

class SyncDataLossError(val missingGameIds: Set<String>): Exception("${missingGameIds.size} games missing from resulting database after merge")