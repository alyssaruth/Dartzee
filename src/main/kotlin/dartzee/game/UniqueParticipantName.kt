package dartzee.game

import dartzee.types.StringMicrotype

/**
 * The team name in a deterministic order - does not vary based on throw order
 */
class UniqueParticipantName(value: String) : StringMicrotype(value)

/**
 * The team name for a particular game - varies based on throw order
 */
class ParticipantName(value: String) : StringMicrotype(value)