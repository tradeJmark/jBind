package ca.tradejmark.jbind.serialization

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.location.ObjectLocation
import kotlinx.coroutines.flow.Flow

interface ObjectProvider<T>: Provider {
    fun getObject(location: ObjectLocation): Flow<T>
}