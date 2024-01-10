package dartzee.sync

class SyncDataLossError(val missingGameIds: Set<String>) :
    Exception("${missingGameIds.size} game(s) missing from resulting database after merge")
