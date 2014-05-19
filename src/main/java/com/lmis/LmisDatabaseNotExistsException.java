package com.lmis;


public class LmisDatabaseNotExistsException extends Exception {

  public LmisDatabaseNotExistsException(String message) {
    super(message);
  }
}
