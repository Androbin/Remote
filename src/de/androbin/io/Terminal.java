package de.androbin.io;

import java.io.*;
import java.util.*;

public final class Terminal {
  private Terminal() {
  }
  
  public static Process exec( final String[] cmdarray, final boolean inheritIO ) {
    final ProcessBuilder process = new ProcessBuilder( cmdarray );
    
    if ( inheritIO ) {
      process.inheritIO();
    }
    
    try {
      return process.start();
    } catch ( final IOException e ) {
      return null;
    }
  }
  
  public static void execAndWait( final String[] cmdarray, final boolean inheritIO ) {
    final Process process = exec( cmdarray, inheritIO );
    
    if ( process == null ) {
      return;
    }
    
    try {
      process.waitFor();
    } catch ( final InterruptedException ignore ) {
      process.destroy();
    }
  }
  
  public static String[] splitCommand( final String command ) {
    final List<String> cmdlist = splitCommandList( command );
    return cmdlist.toArray( new String[ cmdlist.size() ] );
  }
  
  private static List<String> splitCommandList( final String command ) {
    final List<String> cmdlist = new ArrayList<>();
    String cmd = command;
    
    while ( cmd.matches( ".*\".*\".*" ) ) {
      final int openIndex = cmd.indexOf( '\"' );
      final int closeIndex = cmd.indexOf( '\"', openIndex + 1 );
      
      splitSentence( cmd.substring( 0, openIndex ), cmdlist );
      cmdlist.add( cmd.substring( openIndex + 1, closeIndex ) );
      cmd = cmd.substring( closeIndex + 1 );
    }
    
    splitSentence( command, cmdlist );
    return cmdlist;
  }
  
  private static void splitSentence( final String sentence, final List<String> list ) {
    final String[] words = sentence.split( " " );
    
    for ( final String word : words ) {
      list.add( word );
    }
  }
}