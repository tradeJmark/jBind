package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.Location

class UnavailableError (loc: Location): Exception("No resource found at $loc.")