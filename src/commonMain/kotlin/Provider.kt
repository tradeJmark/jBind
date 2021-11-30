package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.BindValueLocation
import kotlinx.coroutines.flow.Flow

interface Provider {
    fun getValue(location: BindValueLocation): Flow<String>
}