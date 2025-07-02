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

package net.labymod.addons.spotify.core.sharing;

import static net.labymod.addons.spotify.core.sharing.TrackSharingController.BROADCAST_DELAY;
import static net.labymod.addons.spotify.core.sharing.TrackSharingController.RESOLVE_DELAY;

import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import de.labystudio.spotifyapi.open.model.track.Image;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import java.util.UUID;
import net.labymod.addons.spotify.core.util.TrackUtil;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.session.Session;
import net.labymod.api.util.Debounce;
import net.labymod.api.util.math.MathHelper;
import net.labymod.api.util.time.TimeUtil;
import org.jetbrains.annotations.Nullable;

public class SharedTrack {

  private final OpenSpotifyAPI openApi;

  private final String trackId;
  private final UUID userId;
  private final boolean isSelf;

  private boolean resolveRequired = true;
  private OpenTrack openTrack;
  private Component component;
  private Icon icon;

  private int position = 0;
  private long timeLastPositionUpdated = 0;

  public SharedTrack(OpenSpotifyAPI openApi, UUID userId, String trackId) {
    this.openApi = openApi;
    this.userId = userId;
    this.trackId = trackId;

    Session session = Laby.labyAPI().minecraft().sessionAccessor().getSession();
    this.isSelf = session != null && session.getUniqueId().equals(userId);
  }

  public void updatePosition(int position) {
    this.position = position;
    this.timeLastPositionUpdated = TimeUtil.getCurrentTimeMillis();
  }

  public String getTrackId() {
    return this.trackId;
  }

  public double getDaemonProgress() {
    if (this.resolveRequired) {
      this.resolve();
    }

    Integer maxPosition = this.openTrack.durationMs;
    if (maxPosition == null || maxPosition <= 0
        || this.position <= 0 || this.position > maxPosition) {
      return 0;
    }

    long elapsedTime = TimeUtil.getCurrentTimeMillis() - this.timeLastPositionUpdated
        + this.getResolveDelay() + BROADCAST_DELAY;
    double progress = (this.position + elapsedTime) / (double) maxPosition;
    return MathHelper.clamp(progress, 0, 1);
  }

  @Nullable
  public Icon getIcon() {
    if (this.resolveRequired) {
      this.resolve();
    }
    return this.icon;
  }

  @Nullable
  public Component getComponent() {
    if (this.resolveRequired) {
      this.resolve();
    }
    return this.component;
  }

  public boolean isExplicit() {
    if (this.resolveRequired) {
      this.resolve();
    }
    return this.openTrack != null && this.openTrack.explicit;
  }

  private void resolve() {
    this.resolveRequired = false;

    Debounce.of("spotifyResolve:" + this.userId.toString(), this.getResolveDelay(), () -> {
      this.openApi.requestOpenTrackAsync(this.trackId, resolvedTrack -> {
        if (resolvedTrack == null) {
          return;
        }
        this.openTrack = resolvedTrack;

        Laby.labyAPI().minecraft().executeOnRenderThread(() -> {
          // Fetch artwork
          Image smallestImage = TrackUtil.getSmallestImage(resolvedTrack);
          if (smallestImage != null) {
            this.icon = Icon.url(smallestImage.url);
          }

          // Fetch name
          String name = resolvedTrack.name;
          int bracketIndex = name.indexOf("(");
          if (bracketIndex != -1 && name.indexOf("Remix", bracketIndex) == -1) {
            name = name.substring(0, bracketIndex);
          }

          if (name.length() > 32) {
            name = name.substring(0, 29) + "...";
          }

          String artist = resolvedTrack.getArtists();
          if (artist.length() > 32) {
            artist = artist.substring(0, 29) + "...";
          }

          String finalName = name.trim();
          String finalArtist = artist.trim();
          this.component = Component.text(finalName + "\n" + finalArtist);
        });
      });
    });
  }

  private long getResolveDelay() {
    return this.isSelf ? 100 : RESOLVE_DELAY;
  }
}
