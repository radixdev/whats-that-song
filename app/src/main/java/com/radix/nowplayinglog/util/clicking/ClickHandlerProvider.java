package com.radix.nowplayinglog.util.clicking;

public class ClickHandlerProvider {
  public ISongClickHandler getAppropriateHandler() {
    return new SpotifyClickHandler();
  }
}
