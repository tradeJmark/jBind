package ca.tradejmark.jbind.websocket

import kotlinx.serialization.Serializable

@Serializable
data class WSProviderError(val msg: String): ClientMessage, ServerMessage
