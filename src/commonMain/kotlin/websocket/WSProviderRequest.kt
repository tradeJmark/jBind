package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.serialization.Serializable

@Serializable
data class WSProviderRequest(val location: ValueLocation): ClientMessage
