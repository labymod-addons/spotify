/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.labymod.addons.spotify.core.util;

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
