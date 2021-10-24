package com.udhd.apiserver.service.feed;

public class FeedException extends Exception {
  public FeedException(String message) {
    super(message);
  }
  static final String ERR_NO_FEED = "error: there is no proper feed.";
}
