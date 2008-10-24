package org.computoring.gop

class GOPException extends RuntimeException {
  GOPException( String message ) {
    super( message )
  }

  GOPException( String message, Throwable cause ) {
    super( message, cause )
  }

  GOPException( Throwable t ) {
    super( t )
  }
}
