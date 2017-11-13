package de.androbin.remote;

import java.io.*;
import java.net.*;

public final class ClientContext implements Closeable {
  public final BufferedReader input;
  public final BufferedWriter output;
  
  private ClientContext( final BufferedReader input, final BufferedWriter output ) {
    this.input = input;
    this.output = output;
  }
  
  @ Override
  public void close() throws IOException {
    input.close();
    output.close();
  }
  
  public static ClientContext of( final Socket client ) {
    final BufferedReader input;
    final BufferedWriter output;
    
    try {
      input = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
      output = new BufferedWriter( new OutputStreamWriter( client.getOutputStream() ) );
    } catch ( final IOException e ) {
      return null;
    }
    
    return new ClientContext( input, output );
  }
}