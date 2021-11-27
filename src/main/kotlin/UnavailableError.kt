package ca.tradejmark.jbind

class UnavailableError(location: BindLoc): Exception("No resource found at $location.")