package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.ObjectLocation
import ca.tradejmark.jbind.location.ValueLocation
import kotlinx.coroutines.flow.StateFlow

interface Provider {
    fun getValue(location: ValueLocation): StateFlow<String?>
    fun getArrayLength(location: ObjectLocation): StateFlow<Int?>
}