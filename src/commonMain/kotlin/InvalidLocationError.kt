package ca.tradejmark.jbind

class InvalidLocationError(loc: String, msg: String): Exception("Location $loc is invalid: $msg")