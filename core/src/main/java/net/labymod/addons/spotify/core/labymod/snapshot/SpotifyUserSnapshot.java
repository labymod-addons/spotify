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
package net.labymod.addons.spotify.core.labymod.snapshot;

import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.addons.spotify.core.SpotifyConfiguration;
import net.labymod.addons.spotify.core.sharing.SharedTrack;
import net.labymod.addons.spotify.core.sharing.TrackSharingController;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.laby3d.renderer.snapshot.AbstractLabySnapshot;
import net.labymod.api.laby3d.renderer.snapshot.Extras;
import org.jetbrains.annotations.Nullable;

public class SpotifyUserSnapshot extends AbstractLabySnapshot {

  private final SharedTrack track;
  private final boolean displayExplicitTracks;
  private final boolean displayTrackCover;

  public SpotifyUserSnapshot(Player player, Extras extras, SpotifyAddon addon) {
    super(extras);
    SpotifyConfiguration configuration = addon.configuration();
    this.track = this.resolveTrack(player, configuration, addon.getController());
    this.displayExplicitTracks = configuration.displayExplicitTracks().get();
    this.displayTrackCover = configuration.displayTrackCover().get();
  }

  public @Nullable SharedTrack getTrack() {
    return this.track;
  }

  public boolean displayExplicitTracks() {
    return this.displayExplicitTracks;
  }

  public boolean displayTrackCover() {
    return this.displayTrackCover;
  }

  private SharedTrack resolveTrack(Player player, SpotifyConfiguration configuration, TrackSharingController controller) {
    if (!configuration.enabled().get()) {
      return null;
    }

    if (!configuration.displayTracks().get()) {
      return null;
    }

    return controller.getTrackOf(player.getUniqueId());
  }
}
