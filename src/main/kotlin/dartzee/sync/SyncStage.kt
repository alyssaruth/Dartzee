package dartzee.sync

enum class SyncStage {
    PULL_REMOTE,
    VALIDATE_REMOTE,
    MERGE_LOCAL_CHANGES,
    UPDATE_ACHIEVEMENTS,
    PUSH_TO_REMOTE,
    PUSH_BACKUP_TO_REMOTE,
    OVERWRITE_LOCAL,
}

fun SyncStage.desc() =
    when (this) {
        SyncStage.PULL_REMOTE -> "Download data"
        SyncStage.VALIDATE_REMOTE -> "Validate database"
        SyncStage.MERGE_LOCAL_CHANGES -> "Merge changes"
        SyncStage.UPDATE_ACHIEVEMENTS -> "Update achievements"
        SyncStage.PUSH_TO_REMOTE -> "Upload new version"
        SyncStage.PUSH_BACKUP_TO_REMOTE -> "Upload backup"
        SyncStage.OVERWRITE_LOCAL -> "Finalise"
    }
