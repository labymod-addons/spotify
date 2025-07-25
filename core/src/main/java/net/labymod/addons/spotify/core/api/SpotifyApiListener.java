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

package net.labymod.addons.spotify.core.api;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;
import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.addons.spotify.core.events.SpotifyConnectEvent;
import net.labymod.addons.spotify.core.events.SpotifyDisconnectEvent;
import net.labymod.addons.spotify.core.events.SpotifyPlaybackChangedEvent;
import net.labymod.addons.spotify.core.events.SpotifyPositionChangedEvent;
import net.labymod.addons.spotify.core.events.SpotifyTrackChangedEvent;
import net.labymod.addons.spotify.core.util.ReconnectDelay;
import net.labymod.api.LabyAPI;

public class SpotifyApiListener implements SpotifyListener {

  private final SpotifyAPI spotifyAPI;
  private final SpotifyAddon spotifyAddon;
  private final LabyAPI labyAPI;

  public SpotifyApiListener(SpotifyAPI spotifyAPI, SpotifyAddon spotifyAddon) {
    this.spotifyAPI = spotifyAPI;
    this.spotifyAddon = spotifyAddon;
    this.labyAPI = spotifyAddon.labyAPI();
  }

  @Override
  public void onConnect() {
    this.labyAPI.eventBus().fire(new SpotifyConnectEvent());
  }

  @Override
  public void onTrackChanged(Track track) {
    this.labyAPI.eventBus().fire(new SpotifyTrackChangedEvent(track));
  }

  @Override
  public void onPositionChanged(int position) {
    Track track = this.spotifyAPI.getTrack();
    this.labyAPI.eventBus().fire(new SpotifyPositionChangedEvent(track, position));

    if (position > 0) {
      this.spotifyAddon.setReconnectDelay(ReconnectDelay.DEFAULT);
    }
  }

  @Override
  public void onPlayBackChanged(boolean isPlaying) {
    Track track = this.spotifyAPI.getTrack();
    this.labyAPI.eventBus().fire(new SpotifyPlaybackChangedEvent(track, isPlaying));
  }

  @Override
  public void onSync() {
    // not needed
  }

  @Override
  public void onDisconnect(Exception exception) {
    this.labyAPI.eventBus().fire(new SpotifyDisconnectEvent());

    this.spotifyAddon.bumpReconnectDelay();
    this.spotifyAddon.initializeSpotifyAndResetDelay(true);
  }
}
