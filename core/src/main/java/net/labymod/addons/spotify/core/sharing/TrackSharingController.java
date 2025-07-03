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

import static net.labymod.addons.spotify.core.util.TrackUtil.isTrackIdValid;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.addons.spotify.core.SpotifyConfiguration;
import net.labymod.addons.spotify.core.events.SpotifyConnectEvent;
import net.labymod.addons.spotify.core.events.SpotifyPlaybackChangedEvent;
import net.labymod.addons.spotify.core.events.SpotifyPositionChangedEvent;
import net.labymod.addons.spotify.core.events.SpotifyTrackChangedEvent;
import net.labymod.api.Laby;
import net.labymod.api.client.session.Session;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.playerinfo.PlayerInfoRemoveEvent;
import net.labymod.api.event.client.world.WorldLeaveEvent;
import net.labymod.api.event.client.world.WorldLoadEvent;
import net.labymod.api.event.labymod.labyconnect.LabyConnectStateUpdateEvent;
import net.labymod.api.event.labymod.labyconnect.session.LabyConnectBroadcastEvent;
import net.labymod.api.event.labymod.labyconnect.session.LabyConnectBroadcastEvent.Action;
import net.labymod.api.labyconnect.LabyConnectSession;
import net.labymod.api.labyconnect.protocol.LabyConnectState;
import net.labymod.api.util.Debounce;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrackSharingController {

  public static final long RESOLVE_DELAY = 1000;
  public static final long BROADCAST_DELAY = 1000;

  private final Map<UUID, SharedTrack> sharedTracks = new HashMap<>();

  private final OpenSpotifyAPI openApi;
  private final SpotifyConfiguration config;
  private final SpotifyAddon spotifyAddon;

  public TrackSharingController(
      OpenSpotifyAPI openApi,
      SpotifyAddon spotifyAddon
  ) {
    this.openApi = openApi;
    this.spotifyAddon = spotifyAddon;
    this.config = spotifyAddon.configuration();

    this.config.enabled().addChangeListener((property, oldValue, newValue) -> {
      this.broadcastCurrentTrack();
    });

    this.config.shareTracks().addChangeListener((property, oldValue, newValue) -> {
      this.broadcastCurrentTrack();
    });

    this.broadcastCurrentTrack();
  }

  private void broadcastCurrentTrack() {
    Debounce.of("spotifyBroadcast", BROADCAST_DELAY, () -> {
      SpotifyAPI spotifyAPI = this.spotifyAddon.getSpotifyAPI();
      Track track = spotifyAPI.getTrack();

      boolean visible = track != null
          && spotifyAPI.isPlaying()
          && this.config.enabled().get()
          && this.config.shareTracks().get();

      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("trackId", visible ? track.getId() : null);
      jsonObject.addProperty("position", spotifyAPI.hasPosition() ? spotifyAPI.getPosition() : -1);

      LabyConnectSession session = Laby.labyAPI().labyConnect().getSession();
      if (session != null && session.isAuthenticated()) {
        session.sendBroadcastPayload("spotify-track-sharing", jsonObject);
      }
    });
  }

  public void updateTrackOf(
      @NotNull UUID uniqueId,
      @Nullable String trackId,
      int position
  ) {
    if (trackId != null && !isTrackIdValid(trackId)) {
      trackId = null;
    }

    if (trackId == null) {
      this.sharedTracks.remove(uniqueId);
      return;
    }

    SharedTrack sharedTrack = this.getTrackOf(uniqueId);
    if (sharedTrack == null || !Objects.equals(sharedTrack.getTrackId(), trackId)) {
      this.sharedTracks.put(
          uniqueId,
          sharedTrack = new SharedTrack(this.openApi, uniqueId, trackId)
      );
    }

    sharedTrack.updatePosition(position);
  }

  @Subscribe
  public void onWorldLoad(WorldLoadEvent event) {
    this.broadcastCurrentTrack();
  }

  @Subscribe
  public void onLabyConnectStateUpdate(LabyConnectStateUpdateEvent event) {
    if (event.state() == LabyConnectState.PLAY) {
      this.broadcastCurrentTrack();
    }
  }

  @Subscribe
  public void onSpotifyConnect(SpotifyConnectEvent event) {
    this.broadcastCurrentTrack();
  }

  @Subscribe
  public void onTrackChanged(SpotifyTrackChangedEvent event) {
    this.broadcastCurrentTrack();
  }

  @Subscribe
  public void onPlaybackChanged(SpotifyPlaybackChangedEvent event) {
    this.broadcastCurrentTrack();
  }

  @Subscribe
  public void onSpotifyPositionChanged(SpotifyPositionChangedEvent event) {
    this.broadcastCurrentTrack();
  }

  @Subscribe
  public void onBroadcastReceive(LabyConnectBroadcastEvent event) {
    @Nullable Session session = Laby.labyAPI().minecraft().sessionAccessor().getSession();
    boolean isSelf = session != null && Objects.equals(session.getUniqueId(), event.getSender());

    if (event.action() != Action.RECEIVE && !isSelf) {
      return;
    }

    if (!event.getKey().equals("spotify-track-sharing")) {
      return;
    }

    JsonElement payload = event.getPayload();
    if (!payload.isJsonObject()) {
      return;
    }

    JsonObject jsonObject = payload.getAsJsonObject();
    String trackId = null;
    int position = -1;

    if (jsonObject.has("trackId") && jsonObject.get("trackId").isJsonPrimitive()) {
      JsonPrimitive primitive = jsonObject.get("trackId").getAsJsonPrimitive();
      if (primitive.isString()) {
        trackId = primitive.getAsString();
      }
    }

    if (jsonObject.has("position") && jsonObject.get("position").isJsonPrimitive()) {
      JsonPrimitive primitive = jsonObject.get("position").getAsJsonPrimitive();
      if (primitive.isNumber()) {
        position = primitive.getAsInt();
      }
    }

    this.updateTrackOf(event.getSender(), trackId, position);
  }

  @Subscribe
  public void onPlayerInfoRemove(PlayerInfoRemoveEvent event) {
    UUID uniqueId = event.playerInfo().profile().getUniqueId();
    this.sharedTracks.remove(uniqueId);
  }

  @Subscribe
  public void onWorldLeave(WorldLeaveEvent event) {
    // Clear all shared tracks when leaving the world
    this.sharedTracks.clear();
  }

  public boolean hasTrack(UUID uniqueId) {
    return this.getTrackOf(uniqueId) != null;
  }

  public SharedTrack getTrackOf(UUID uniqueId) {
    return this.sharedTracks.get(uniqueId);
  }

}
