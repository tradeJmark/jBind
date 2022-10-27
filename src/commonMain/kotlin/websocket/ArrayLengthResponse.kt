package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.location.ObjectLocation
import kotlinx.serialization.Serializable

@Serializable
data class ArrayLengthResponse(val location: ObjectLocation, val length: Int): ServerMessage
