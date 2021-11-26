package ca.tradejmark.jbind

import kotlinx.coroutines.flow.Flow

interface Provider {
    fun getString(location: BindLoc): Flow<String>
}