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

package net.labymod.addons.spotify.core;

import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class OpenSpotifyAPIWrapper {

  private final OpenSpotifyAPI openSpotifyAPI;
  private final Map<String, List<Consumer<OpenTrack>>> pendingRequests = new HashMap<>();

  public OpenSpotifyAPIWrapper(OpenSpotifyAPI openSpotifyAPI) {
    this.openSpotifyAPI = openSpotifyAPI;
  }

  public void get(String trackId, Consumer<OpenTrack> callback) {
    List<Consumer<OpenTrack>> resultCallbacks = this.pendingRequests.get(trackId);
    if (resultCallbacks != null) {
      resultCallbacks.add(callback);
      return;
    }

    resultCallbacks = new ArrayList<>();
    resultCallbacks.add(callback);
    this.pendingRequests.put(trackId, resultCallbacks);
    try {
      this.openSpotifyAPI.requestOpenTrackAsync(trackId, opentrack -> {
        if (opentrack == null) {
          return;
        }

        List<Consumer<OpenTrack>> callbacks = this.pendingRequests.remove(trackId);
        if (callbacks != null) {
          for (Consumer<OpenTrack> openTrackConsumer : callbacks) {
            openTrackConsumer.accept(opentrack);
          }
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
