package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.location.BindValueLocation
import kotlinx.serialization.Serializable

@Serializable
data class WSProviderResponse(val location: BindValueLocation, val value: String): ServerMessage
