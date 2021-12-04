package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.BindObjectLocation
import ca.tradejmark.jbind.location.BindPath
import ca.tradejmark.jbind.location.BindValueLocation

class UnavailableError private constructor(loc: String): Exception("No resource found at $loc.") {
    constructor(location: BindValueLocation): this(location.toString())
    constructor(location: BindObjectLocation): this(location.toString())
    constructor(location: BindPath): this(location.toString())
}