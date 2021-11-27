package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.BindLoc

class UnavailableError(location: BindLoc): Exception("No resource found at $location.")