package de.androbin.remote;

import java.io.*;

public interface Handle {
  void handleInput( ServerContext server, BufferedReader client );
  
  void handleOutput( ServerContext server, BufferedWriter client );
  
  boolean isRunning();
  
  boolean isTerminal();
  
  void start();
  
  void stop();
}