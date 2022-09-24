package ca.tradejmark.jbind.serialization

import ca.tradejmark.jbind.Provider
import ca.tradejmark.jbind.location.BindObjectLocation
import kotlinx.coroutines.flow.Flow

interface ObjectProvider<T>: Provider {
    fun getObject(location: BindObjectLocation): Flow<T>
}