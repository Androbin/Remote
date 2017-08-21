package de.androbin.remote;

import de.androbin.logging.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public final class Remote {
  public PrintStream journal;
  
  public final Commander commander;
  
  private ServerSocket server;
  private boolean serverRunning;
  
  public Remote() {
    this.commander = new Commander();
  }
  
  private void handle( final Socket client, final DataInputStream input ) {
    boolean clientRunning = true;
    boolean terminate = false;
    
    while ( clientRunning ) {
      final String instruction;
      
      try {
        instruction = input.readUTF();
      } catch ( final IOException e ) {
        clientRunning = false;
        continue;
      }
      
      log( "\t\t<instruction>" + instruction + "</instruction>" );
      
      switch ( instruction ) {
        case "terminate":
          terminate = true;
        case "disconnect":
          clientRunning = false;
          continue;
      }
      
      commander.execute( instruction );
    }
    
    try {
      input.close();
      client.close();
      log( "\t<client disconnected>" );
    } catch ( final IOException e ) {
      log( "\t<client disconnect failed>" );
    }
    
    if ( terminate ) {
      stopServer();
    }
  }
  
  private void log( final String entry ) {
    final PrintStream journal = this.journal;
    
    if ( journal != null ) {
      journal.println( entry );
    }
  }
  
  public static void main( final String[] args ) {
    if ( !GraphicsEnvironment.isHeadless() ) {
      LoggingPanel.inWindow( "Remote Logger", 1600, 900 ).setVisible( true );
    }
    
    final Remote remote = new Remote();
    remote.journal = System.out;
    remote.commander.defaultMapping = command -> Terminal
        .execAndWait( Terminal.splitCommand( command ) );
    remote.startServer( 2823 );
  }
  
  @ SuppressWarnings( "resource" )
  private void mediate() {
    final ExecutorService clientHandler = Executors.newCachedThreadPool();
    
    while ( serverRunning ) {
      final Socket client;
      final DataInputStream input;
      
      try {
        client = server.accept();
        input = new DataInputStream( client.getInputStream() );
        log( "\t<client connected>" );
      } catch ( final IOException e ) {
        continue;
      }
      
      clientHandler.execute( () -> handle( client, input ) );
    }
    
    clientHandler.shutdown();
  }
  
  public void startServer( final int port ) {
    try {
      server = new ServerSocket( port );
      log( "<server started>" );
      serverRunning = true;
    } catch ( final IOException e ) {
      log( "<server start failed>" );
      return;
    }
    
    new Thread( this::mediate, "Client Mediator" ).start();
  }
  
  public void stopServer() {
    serverRunning = false;
    
    try {
      server.close();
      log( "<server stopped>" );
    } catch ( final IOException e ) {
      log( "<server stop failed>" );
    }
  }
}