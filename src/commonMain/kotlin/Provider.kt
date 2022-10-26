package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.coroutines.flow.Flow

interface Provider {
    fun getValue(location: ValueLocation): Flow<String>
}