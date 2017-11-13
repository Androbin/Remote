package de.androbin.remote;

import java.io.*;

public final class ServerContext {
  public PrintStream journal;
  
  public void log( final String entry ) {
    final PrintStream journal = this.journal;
    
    if ( journal != null ) {
      journal.println( entry );
    }
  }
}