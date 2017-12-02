package de.androbin.remote;

public interface Handle {
  void handleInput( ServerContext server, ClientContext client );
  
  void handleOutput( ServerContext server, ClientContext client );
  
  boolean isRunning();
  
  boolean isTerminal();
  
  void start();
  
  void stop();
}