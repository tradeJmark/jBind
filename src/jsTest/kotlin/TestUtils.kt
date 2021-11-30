package ca.tradejmark.jbind

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object TestUtils {
    suspend fun delayForUpdate() {
        withContext(Dispatchers.Default) { delay(10L) }
    }
}