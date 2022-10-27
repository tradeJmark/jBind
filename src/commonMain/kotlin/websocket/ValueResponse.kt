package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.serialization.Serializable

@Serializable
data class ValueResponse(val location: ValueLocation, val value: String): ServerMessage
