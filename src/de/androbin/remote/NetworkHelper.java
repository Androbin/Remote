package de.androbin.remote;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public final class NetworkHelper implements Runnable {
  private static final String IP_REGEX = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
  private static final String PORT_REGEX = "(102[4-9]|10[3-9][0-9]|1[1-9][0-9]{2}|[2-9][0-9]{3}|[1-3][0-9]{4}|4[0-8][0-9]{3}|490[0-9]{2}|491[0-4][0-9]|4915[01])";
  
  private Thread thread;
  private volatile boolean running;
  
  private final BlockingQueue<String> queue;
  
  private Socket server;
  private DataOutputStream outputStream;
  
  public NetworkHelper() {
    this.queue = new LinkedBlockingQueue<>();
  }
  
  public static boolean checkAddress( final String address ) {
    final String regex = IP_REGEX + ":" + PORT_REGEX;
    return address.matches( regex );
  }
  
  @ Override
  public void run() {
    while ( running ) {
      final String message;
      
      try {
        message = queue.take();
      } catch ( final InterruptedException e ) {
        continue;
      }
      
      try {
        outputStream.writeUTF( message );
      } catch ( final IOException ignore ) {
      }
    }
  }
  
  public void send( final String message, final boolean keep ) {
    if ( !running && !keep ) {
      return;
    }
    
    try {
      queue.put( message );
    } catch ( final InterruptedException ignore ) {
    }
  }
  
  public boolean start( final String ip, final int port ) {
    if ( running ) {
      return true;
    }
    try {
      server = new Socket( ip, port );
      outputStream = new DataOutputStream( server.getOutputStream() );
    } catch ( final IOException e ) {
      return false;
    }
    
    running = true;
    thread = new Thread( this, "NetworkHelper" );
    thread.start();
    return true;
  }
  
  public boolean stop() {
    if ( !running ) {
      return true;
    }
    
    running = false;
    thread.interrupt();
    thread = null;
    
    try {
      server.close();
    } catch ( final IOException e ) {
      return false;
    }
    
    return true;
  }
}