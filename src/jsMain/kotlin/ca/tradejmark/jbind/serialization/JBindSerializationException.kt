package ca.tradejmark.jbind.serialization

import kotlinx.serialization.SerializationException

class JBindSerializationException(msg: String): SerializationException(msg)