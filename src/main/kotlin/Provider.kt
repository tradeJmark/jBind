package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.BindLoc
import kotlinx.coroutines.flow.Flow

interface Provider {
    fun getString(location: BindLoc): Flow<String>
}