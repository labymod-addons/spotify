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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Cache<T> {

  private final Map<String, T> cache = new ConcurrentHashMap<>();
  private final List<String> cacheQueue = new ArrayList<>();
  private int cacheSize;

  private final Consumer<T> onEntryRemoved;

  public Cache(int cacheSize, Consumer<T> onEntryRemoved) {
    this.cacheSize = cacheSize;
    this.onEntryRemoved = onEntryRemoved;
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  public void push(String key, T value) {
    // Remove entry from cache if cache is full
    if (this.cacheQueue.size() > this.cacheSize) {
      String urlToRemove = this.cacheQueue.remove(0);
      this.cache.remove(urlToRemove);
      this.onEntryRemoved.accept(value);
    }

    // Add new entry to cache
    this.cache.put(key, value);
    this.cacheQueue.add(key);
  }

  public boolean has(String key) {
    return this.cache.containsKey(key);
  }

  public T get(String key) {
    return this.cache.get(key);
  }

  public void clear() {
    this.cache.clear();
    this.cacheQueue.clear();
  }

}