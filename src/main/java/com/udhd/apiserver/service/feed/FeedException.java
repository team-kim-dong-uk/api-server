package com.udhd.apiserver.service.feed;

public class FeedException extends Exception {

  static final String ERR_NO_FEED = "error: there is no proper feed.";

  public FeedException(String message) {
    super(message);
  }
}
