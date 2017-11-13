package de.androbin.remote;

public interface Handle {
  boolean handle( ServerContext server, ClientContext client );
  
  boolean isTerminal();
  
  void start();
  
  void stop();
}