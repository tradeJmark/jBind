package ca.tradejmark.jbind.websocket

import ca.tradejmark.jbind.location.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

sealed interface Message
sealed interface ClientMessage: Message
sealed interface ServerMessage: Message

object Serialization {
    private val json = Json {
        serializersModule = SerializersModule {
            fun PolymorphicModuleBuilder<ServerMessage>.registerServerMessages() {
                subclass(WSProviderResponse::class)
                subclass(WSProviderError::class)
            }
            fun PolymorphicModuleBuilder<ClientMessage>.registerClientMessages() {
                subclass(WSProviderRequest::class)
                subclass(WSProviderError::class)
            }
            fun PolymorphicModuleBuilder<ObjectLikeLocation>.registerObjectLikeLocations() {
                subclass(ObjectLocation::class)
                subclass(ArrayItemLocation::class)
                subclass(RelativeObjectLocation::class)
                subclass(RelativeArrayItemLocation::class)
            }
            fun PolymorphicModuleBuilder<PathLikeLocation>.registerPathLikeLocations() {
                subclass(Path::class)
                subclass(RelativePath::class)
            }
            polymorphic(Message::class) {
                registerServerMessages()
                registerClientMessages()
            }
            polymorphic(ServerMessage::class) {
                registerServerMessages()
            }
            polymorphic(ClientMessage::class) {
                registerClientMessages()
            }
            polymorphic(Location::class) {
                registerObjectLikeLocations()
                registerPathLikeLocations()
                subclass(ValueLocation::class)
            }
            polymorphic(ObjectLikeLocation::class) {
                registerObjectLikeLocations()
            }
            polymorphic(PathLikeLocation::class) {
                registerPathLikeLocations()
            }
        }
    }
    fun serializeMessage(message: Message): String = json.encodeToString(message)
    fun deserializeClientMessage(message: String): ClientMessage = json.decodeFromString(message)
    fun deserializeServerMessage(message: String): ServerMessage = json.decodeFromString(message)
}