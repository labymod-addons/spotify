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
package net.labymod.addons.spotify.core.labymod.interaction;

import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.addons.spotify.core.SpotifyConfiguration;
import net.labymod.addons.spotify.core.sharing.SharedTrack;
import net.labymod.addons.spotify.core.sharing.TrackSharingController;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.entity.player.interaction.AbstractBulletPoint;
import net.labymod.api.models.OperatingSystem;

public class SpotifyTrackBulletPoint extends AbstractBulletPoint {

  private static final String URL = "https://open.spotify.com/track/%s?si=labymod_spotify";

  private final SpotifyAddon spotifyAddon;
  private final TrackSharingController broadcastController;

  public SpotifyTrackBulletPoint(
      SpotifyAddon spotifyAddon,
      TrackSharingController broadcastController
  ) {
    super(Component.translatable("spotify.bulletPoint.open.name"));

    this.spotifyAddon = spotifyAddon;
    this.broadcastController = broadcastController;
  }

  @Override
  public void execute(Player player) {
    SharedTrack track = this.broadcastController.getTrackOf(player.getUniqueId());
    if (track == null) {
      return;
    }
    OperatingSystem.getPlatform().openUri(String.format(URL, track.getTrackId()));
  }

  @Override
  public boolean isVisible(Player player) {
    SpotifyConfiguration configuration = this.spotifyAddon.configuration();
    if (!configuration.enabled().get() || !configuration.displayTracks().get()) {
      return false;
    }
    return this.broadcastController.hasTrack(player.getUniqueId());
  }
}
