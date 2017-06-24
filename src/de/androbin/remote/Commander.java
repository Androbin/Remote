package de.androbin.remote;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public final class Commander {
  private final ExecutorService executor;
  public final Map<String, Consumer<String>> mappings;
  public Consumer<String> defaultMapping;
  
  public Commander() {
    this.executor = Executors.newCachedThreadPool();
    this.mappings = new HashMap<>();
  }
  
  public void execute( final String instruction ) {
    final int split = instruction.indexOf( ' ' );
    
    final String command;
    final String args;
    
    if ( split >= 0 ) {
      command = instruction.substring( 0, split );
      args = instruction.substring( split + 1 );
    } else {
      command = instruction;
      args = "";
    }
    
    final Consumer<String> mapping = mappings.getOrDefault( command, defaultMapping );
    executor.execute( () -> mapping.accept( args ) );
  }
  
  public void shutdown() {
    executor.shutdown();
  }
}