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

package net.labymod.addons.spotify.core.listener;

import de.labystudio.spotifyapi.SpotifyAPI;
import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.addons.spotify.core.events.SpotifyPlaybackChangedEvent;
import net.labymod.addons.spotify.core.misc.BroadcastController;
import net.labymod.api.LabyAPI;
import net.labymod.api.client.entity.player.ClientPlayer;
import net.labymod.api.event.Subscribe;

public class SpotifyPlaybackChangedListener {

  private final SpotifyAddon spotifyAddon;
  private final SpotifyAPI spotifyAPI;
  private final BroadcastController broadcastController;

  public SpotifyPlaybackChangedListener(
      SpotifyAddon spotifyAddon,
      SpotifyAPI spotifyAPI,
      BroadcastController broadcastController
  ) {
    this.spotifyAddon = spotifyAddon;
    this.spotifyAPI = spotifyAPI;
    this.broadcastController = broadcastController;
    spotifyAddon.configuration().enabled().addChangeListener((property, oldValue, newValue) -> {
      this.refreshTrack(newValue);
    });

    spotifyAddon.configuration().shareTracks().addChangeListener((property, oldValue, newValue) -> {
      this.refreshTrack(newValue);
    });
  }

  @Subscribe
  public void onPlaybackChanged(SpotifyPlaybackChangedEvent event) {
    if (!this.spotifyAddon.configuration().shareTracks().get()) {
      return;
    }

    this.refreshTrack(event.isPlaying());
  }

  private void refreshTrack(boolean playing) {
    if (playing && !this.spotifyAPI.hasTrack()) {
      return;
    }

    String trackId = playing ? this.spotifyAPI.getTrack().getId() : null;
    LabyAPI labyAPI = this.spotifyAddon.labyAPI();
    this.broadcastController.receive(labyAPI.getUniqueId(), trackId);
    ClientPlayer clientPlayer = labyAPI.minecraft().clientPlayer();
    if (clientPlayer == null) {
      return;
    }

    this.broadcastController.queue(trackId == null ? "null" : trackId);
  }
}
