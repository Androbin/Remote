package de.androbin.remote;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public final class Commander implements Runnable {
  public final Map<String, Consumer<String>> mappings;
  public Consumer<String> defaultMapping;
  
  private Thread thread;
  private volatile boolean running;
  
  private final BlockingQueue<String> queue;
  
  public Commander() {
    this.mappings = new HashMap<>();
    this.queue = new LinkedBlockingQueue<>();
  }
  
  private void execute( final String instruction ) {
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
    
    if ( mappings.containsKey( command ) ) {
      mappings.get( command ).accept( args );
    } else if ( defaultMapping != null ) {
      defaultMapping.accept( instruction );
    }
  }
  
  public void interrupt() {
    if ( !running ) {
      return;
    }
    
    thread.interrupt();
  }
  
  @ Override
  public void run() {
    while ( running ) {
      final String instruction;
      
      try {
        instruction = queue.take();
      } catch ( final InterruptedException e ) {
        continue;
      }
      
      execute( instruction );
    }
  }
  
  public void start() {
    if ( running ) {
      return;
    }
    
    running = true;
    thread = new Thread( this, "Commander" );
    thread.setDaemon( true );
    thread.start();
  }
  
  public void stop() {
    if ( !running ) {
      return;
    }
    
    running = false;
    thread.interrupt();
    thread = null;
  }
  
  public void submit( final String instruction ) {
    queue.add( instruction );
  }
}