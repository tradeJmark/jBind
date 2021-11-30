package ca.tradejmark.jbind

import ca.tradejmark.jbind.location.BindValueLocation

class UnavailableError(location: BindValueLocation): Exception("No resource found at $location.")