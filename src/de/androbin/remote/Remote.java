package de.androbin.remote;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.function.*;

public final class Remote {
  public final ServerContext context;
  public final Supplier<Handle> origin;
  
  private ServerSocket server;
  private boolean running;
  
  public Remote( final Supplier<Handle> origin ) {
    this.context = new ServerContext();
    this.origin = origin;
  }
  
  private void handle( final Socket client ) {
    final ClientContext clientContext = ClientContext.of( client );
    
    final Handle handle = origin.get();
    handle.start();
    
    final Thread inputThread = new Thread( () -> {
      while ( handle.isRunning() ) {
        handle.handleInput( context, clientContext.input );
      }
    }, "Handle Input" );
    inputThread.setDaemon( true );
    inputThread.start();
    
    final Thread outputThread = new Thread( () -> {
      while ( handle.isRunning() ) {
        handle.handleOutput( context, clientContext.output );
      }
    }, "Handle Output" );
    outputThread.setDaemon( true );
    outputThread.start();
    
    synchronized ( inputThread ) {
      try {
        inputThread.wait();
      } catch ( final InterruptedException ignore ) {
      }
    }
    
    outputThread.interrupt();
    
    synchronized ( outputThread ) {
      try {
        outputThread.wait();
      } catch ( final InterruptedException ignore ) {
      }
    }
    
    final boolean terminate = handle.isTerminal();
    
    try {
      clientContext.close();
      client.close();
      context.log( "\t</client>" );
    } catch ( final IOException e ) {
      context.log( "\t<!client-stop failed>" );
    }
    
    if ( terminate ) {
      stopServer();
    }
  }
  
  @ SuppressWarnings( "resource" )
  private void mediate() {
    final ExecutorService handler = Executors.newCachedThreadPool();
    
    while ( running ) {
      final Socket client;
      
      try {
        client = server.accept();
        context.log( "\t<client>" );
      } catch ( final IOException e ) {
        context.log( "\t<!client-start failed>" );
        continue;
      }
      
      handler.execute( () -> handle( client ) );
    }
    
    handler.shutdown();
  }
  
  public void startServer( final int port ) {
    try {
      server = new ServerSocket( port );
      context.log( "<server>" );
      running = true;
    } catch ( final IOException e ) {
      context.log( "<!server-start failed>" );
      return;
    }
    
    new Thread( this::mediate, "Client Mediator" ).start();
  }
  
  public void stopServer() {
    running = false;
    
    try {
      server.close();
      context.log( "</server>" );
    } catch ( final IOException e ) {
      context.log( "<!server-stop failed>" );
    }
  }
}