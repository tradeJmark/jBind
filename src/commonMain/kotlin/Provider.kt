package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.coroutines.flow.Flow

interface Provider {
    fun getValue(location: ValueLocation): Flow<String>

    fun getArrayLength(location: ObjectLocation): Flow<Int>
}