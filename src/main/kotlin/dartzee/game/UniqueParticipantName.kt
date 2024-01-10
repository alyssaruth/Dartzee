package dartzee.game

import dartzee.types.StringMicrotype

/** The team name in a deterministic order - does not vary based on throw order */
class UniqueParticipantName(value: String) : StringMicrotype(value)
