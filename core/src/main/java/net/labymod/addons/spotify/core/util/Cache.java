package net.labymod.addons.spotify.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Cache<T> {

  private final Map<String, CacheEntry<T>> cache = new ConcurrentHashMap<>();

  private final long maxLifetime;
  private final Consumer<T> onEntryRemoved;

  public Cache(long maxLifetime, Consumer<T> onEntryRemoved) {
    this.maxLifetime = maxLifetime;
    this.onEntryRemoved = onEntryRemoved;
  }

  public void push(String key, T value) {
    this.cache.put(key, new CacheEntry<>(value));
    this.cleanup();
  }

  public boolean has(String key) {
    CacheEntry<T> entry = this.cache.get(key);
    if (entry != null && !entry.isExpired()) {
      entry.updateAccessTime();
      return true;
    }
    return false;
  }

  public T get(String key) {
    CacheEntry<T> entry = this.cache.get(key);
    if (entry != null && !entry.isExpired()) {
      entry.updateAccessTime();
      return entry.value;
    }
    return null;
  }

  public void clear() {
    for (CacheEntry<T> entry : this.cache.values()) {
      this.onEntryRemoved.accept(entry.value);
    }
    this.cache.clear();
  }

  private void cleanup() {
    for (Map.Entry<String, CacheEntry<T>> entry : this.cache.entrySet()) {
      if (entry.getValue().isExpired()) {
        this.cache.remove(entry.getKey());
        this.onEntryRemoved.accept(entry.getValue().value);
      }
    }
  }

  private class CacheEntry<K> {

    K value;
    long lastAccessed;

    CacheEntry(K value) {
      this.value = value;
      this.lastAccessed = System.currentTimeMillis();
    }

    void updateAccessTime() {
      this.lastAccessed = System.currentTimeMillis();
    }

    boolean isExpired() {
      return System.currentTimeMillis() - this.lastAccessed > Cache.this.maxLifetime;
    }
  }
}
