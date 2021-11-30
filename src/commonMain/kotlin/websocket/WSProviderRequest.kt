package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.location.BindValueLocation
import kotlinx.serialization.Serializable

@Serializable
data class WSProviderRequest(val location: BindValueLocation): ClientMessage
