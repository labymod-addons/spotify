package net.labymod.addons.spotify.core.misc;

import java.util.concurrent.TimeUnit;

public enum ReconnectDelay {
  DEFAULT(10, TimeUnit.SECONDS),
  FIRST(30, TimeUnit.SECONDS),
  SECOND(60, TimeUnit.SECONDS),
  THIRD(120, TimeUnit.SECONDS),
  FOURTH(300, TimeUnit.SECONDS),
  FIFTH(600, TimeUnit.SECONDS),
  SIXTH(900, TimeUnit.SECONDS),
  ;

  private static final ReconnectDelay[] VALUES = values();

  private final long delay;

  ReconnectDelay(int delay, TimeUnit timeUnit) {
    this.delay = timeUnit.toMillis(delay);
  }

  public static ReconnectDelay of(long delay) {
    for (ReconnectDelay value : VALUES) {
      if (value.getDelay() == delay) {
        return value;
      }
    }

    return DEFAULT;
  }

  public long getDelay() {
    return this.delay;
  }

  public ReconnectDelay next() {
    return this.ordinal() == VALUES.length - 1 ? SIXTH : VALUES[this.ordinal() + 1];
  }
}
