package com.udhd.apiserver.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/")
@Controller
public class AppController {

  @ResponseStatus(HttpStatus.OK)
  @RequestMapping("healthcheck")
  public void healthCheck() {
    return;
  }

  @ResponseStatus(HttpStatus.OK)
  @RequestMapping("favicon.ico")
  public void favicon() {
    return;
  }
}
