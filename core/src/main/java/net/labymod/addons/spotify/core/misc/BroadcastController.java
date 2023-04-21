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

package net.labymod.addons.spotify.core.misc;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import de.labystudio.spotifyapi.open.model.track.Image;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.labymod.addons.spotify.core.OpenSpotifyAPIWrapper;
import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.labyconnect.LabyConnectSession;
import net.labymod.api.util.concurrent.task.Task;

public class BroadcastController {

  private final OpenSpotifyAPIWrapper openSpotifyAPI;
  private final SpotifyAddon spotifyAddon;
  private final List<ReceivedBroadcast> receivedBroadcasts;

  private String lastQueuedBroadcast;
  private String queuedBroadcast;
  private Task queuedBroadcastTask;


  public BroadcastController(OpenSpotifyAPIWrapper openSpotifyAPI, SpotifyAddon spotifyAddon) {
    this.openSpotifyAPI = openSpotifyAPI;
    this.spotifyAddon = spotifyAddon;
    this.receivedBroadcasts = new ArrayList<>();
  }

  public void queue(String trackId) {
    this.queuedBroadcast = trackId;
    this.updateTask();
  }

  public void remove(UUID potentialListener) {
    ReceivedBroadcast broadcast = this.getBroadcast(potentialListener);
    if (broadcast == null) {
      return;
    }

    this.receivedBroadcasts.remove(broadcast);
    Icon icon = broadcast.icon;
    if (icon == null) {
      return;
    }

    ResourceLocation resourceLocation = icon.getResourceLocation();
    if (resourceLocation == null) {
      return;
    }

    Laby.references().textureRepository().queueTextureRelease(resourceLocation);
  }

  public void receive(UUID sender, String trackId) {
    ReceivedBroadcast broadcast = this.getBroadcast(sender);
    if (broadcast == null) {
      if (trackId != null) {
        this.receivedBroadcasts.add(new ReceivedBroadcast(sender, trackId));
      }

      return;
    }

    broadcast.updateTrack(trackId);
  }

  public ReceivedBroadcast get(UUID uniqueId) {
    ReceivedBroadcast broadcast = this.getBroadcast(uniqueId);
    if (broadcast == null) {
      return null;
    }

    if (broadcast.track == null && !broadcast.loading) {
      broadcast.loadTrack();
    }

    if (broadcast.component != null) {
      return broadcast;
    }

    return null;
  }

  private ReceivedBroadcast getBroadcast(UUID uniqueId) {
    for (ReceivedBroadcast receivedBroadcast : this.receivedBroadcasts) {
      if (Objects.equals(receivedBroadcast.uniqueId, uniqueId)) {
        return receivedBroadcast;
      }
    }

    return null;
  }

  private void updateTask() {
    if (this.queuedBroadcastTask != null && this.queuedBroadcastTask.isRunning()) {
      this.queuedBroadcastTask.cancel();
    }

    this.queuedBroadcastTask = Task.builder(() -> {
      if (this.queuedBroadcast == null) {
        return;
      }

      String trackId = this.queuedBroadcast;
      this.queuedBroadcast = null;
      if (trackId.equals(this.lastQueuedBroadcast)) {
        return;
      }

      this.lastQueuedBroadcast = trackId;
      LabyConnectSession session = Laby.labyAPI().labyConnect().getSession();
      if (session == null) {
        return;
      }

      JsonObject jsonObject = new JsonObject();
      if (trackId.equals("null")) {
        jsonObject.add("trackId", JsonNull.INSTANCE);
      } else {
        jsonObject.addProperty("trackId", trackId);
      }

      session.sendBroadcastPayload("spotify-track-sharing", jsonObject);
    }).delay(10, TimeUnit.SECONDS).build();
    this.queuedBroadcastTask.executeOnRenderThread();
  }

  public class ReceivedBroadcast {

    private final UUID uniqueId;
    public OpenTrack track;
    public Component component;
    public Icon icon;
    private String trackId;
    private boolean loading;

    private ReceivedBroadcast(UUID uniqueId, String trackId) {
      this.uniqueId = uniqueId;
      this.updateTrack(trackId);
    }

    public void updateTrack(String trackId) {
      if (this.icon != null) {
        ResourceLocation resourceLocation = this.icon.getResourceLocation();
        if (resourceLocation != null) {
          Laby.references().textureRepository().queueTextureRelease(resourceLocation);
        }
      }

      this.trackId = trackId;
      this.track = null;

      if (trackId == null) {
        this.component = null;
      }
    }

    private void loadTrack() {
      if (this.trackId == null) {
        return;
      }

      this.loading = true;
      BroadcastController.this.openSpotifyAPI.get(this.trackId, openTrack -> {
        if (openTrack == null || openTrack.artists == null || openTrack.artists.isEmpty()) {
          return;
        }

        this.loading = false;
        if (openTrack.explicit && !BroadcastController.this.spotifyAddon.configuration()
            .displayExplicitTracks().get()) {
          this.component = null;
          return;
        }

        this.track = openTrack;
        String name = openTrack.name;
        int bracketIndex = name.indexOf("(");
        if (bracketIndex != -1 && name.indexOf("Remix", bracketIndex) == -1) {
          name = name.substring(0, bracketIndex);
        }

        if (name.length() > 32) {
          name = name.substring(0, 29) + "...";
        }

        String artist = openTrack.getArtists();
        if (artist.length() > 32) {
          artist = artist.substring(0, 29) + "...";
        }

        String finalName = name.trim();
        String finalArtist = artist.trim();
        Laby.labyAPI().minecraft().executeOnRenderThread(
            () -> this.component = Component.text(finalName + "\n" + finalArtist)
        );

        if (!BroadcastController.this.spotifyAddon.configuration().displayTrackCover().get()
            || openTrack.album.images == null || openTrack.album.images.isEmpty()) {
          this.icon = null;
          return;
        }

        Image smallestImage = null;
        for (Image image : openTrack.album.images) {
          if (smallestImage == null || image.width < smallestImage.width) {
            smallestImage = image;
          }
        }

        this.icon = Icon.url(smallestImage.url);
      });
    }

    public String getTrackId() {
      return this.trackId;
    }
  }
}
