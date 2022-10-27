package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.location.ObjectLocation
import kotlinx.serialization.Serializable

@Serializable
data class ArrayLengthRequest(val location: ObjectLocation): ClientMessage
