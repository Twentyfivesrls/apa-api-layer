package com.twentyfive.apaapilayer.exceptions;

public class SiteIsClosedException extends RuntimeException {
  public SiteIsClosedException(String message) {
    super(message);
  }
}
