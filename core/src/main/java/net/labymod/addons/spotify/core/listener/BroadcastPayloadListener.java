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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.addons.spotify.core.misc.BroadcastController;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.labymod.labyconnect.session.LabyConnectBroadcastEvent;
import net.labymod.api.event.labymod.labyconnect.session.LabyConnectBroadcastEvent.Action;

public class BroadcastPayloadListener {

  private final SpotifyAddon spotifyAddon;
  private final BroadcastController broadcastController;

  public BroadcastPayloadListener(SpotifyAddon spotifyAddon,
      BroadcastController broadcastController) {
    this.spotifyAddon = spotifyAddon;
    this.broadcastController = broadcastController;
  }

  @Subscribe
  public void onBroadcastReceive(LabyConnectBroadcastEvent event) {
    if (event.action() != Action.RECEIVE) {
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
    if (jsonObject.has("trackId") && jsonObject.get("trackId").isJsonPrimitive()) {
      JsonPrimitive primitive = jsonObject.get("trackId").getAsJsonPrimitive();
      if (primitive.isString()) {
        trackId = primitive.getAsString();
      }
    }

    if (trackId == null || this.spotifyAddon.configuration().displayTracks().get()) {
      this.broadcastController.receive(event.getSender(), trackId);
    }
  }
}
