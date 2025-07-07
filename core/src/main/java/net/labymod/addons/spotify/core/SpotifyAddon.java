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

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import de.labystudio.spotifyapi.open.totp.provider.SecretProvider;
import net.labymod.addons.spotify.core.api.HttpSecretProvider;
import net.labymod.addons.spotify.core.api.SpotifyApiListener;
import net.labymod.addons.spotify.core.labymod.hudwidgets.SpotifyHudWidget;
import net.labymod.addons.spotify.core.labymod.hudwidgets.SpotifyTextHudWidget;
import net.labymod.addons.spotify.core.labymod.interaction.SpotifyTrackBulletPoint;
import net.labymod.addons.spotify.core.labymod.nametag.SpotifySharedTrack;
import net.labymod.addons.spotify.core.sharing.TrackSharingController;
import net.labymod.addons.spotify.core.util.ReconnectDelay;
import net.labymod.api.Constants.Files;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.entity.player.interaction.InteractionMenuRegistry;
import net.labymod.api.client.entity.player.tag.PositionType;
import net.labymod.api.client.gui.hud.HudWidgetRegistry;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class SpotifyAddon extends LabyAddon<SpotifyConfiguration> {

  private final Icon hudIcon = Icon.texture(ResourceLocation.create(
      "spotify",
      "themes/vanilla/textures/settings/hud/spotify32.png"
  )).resolution(64, 64);

  private static SpotifyAddon instance;
  private final SpotifyAPI spotifyAPI;

  private ReconnectDelay reconnectDelay = ReconnectDelay.DEFAULT;

  public SpotifyAddon() {
    SpotifyAddon.instance = this;
    this.spotifyAPI = SpotifyAPIFactory.create();
  }

  @Override
  protected void enable() {
    this.registerSettingCategory();

    SpotifyApiListener spotifyApiListener = new SpotifyApiListener(this.spotifyAPI, this);
    this.spotifyAPI.registerListener(spotifyApiListener);

    this.initializeSpotifyAndResetDelay();

    SecretProvider secretProvider = new HttpSecretProvider();
    OpenSpotifyAPI openApi = new OpenSpotifyAPI(secretProvider);

    HudWidgetRegistry registry = this.labyAPI().hudWidgetRegistry();
    registry.register(new SpotifyTextHudWidget("spotify_track", this.hudIcon, this.spotifyAPI));
    registry.register(new SpotifyHudWidget(
        "spotify",
        this.hudIcon,
        this,
        openApi,
        this.spotifyAPI
    ));

    TrackSharingController controller = new TrackSharingController(openApi, this);
    this.labyAPI().eventBus().registerListener(controller);

    InteractionMenuRegistry menuRegistry = this.labyAPI().interactionMenuRegistry();
    menuRegistry.register(new SpotifyTrackBulletPoint(this, controller));

    this.labyAPI().tagRegistry().register(
        "spotify_shared_track",
        PositionType.BELOW_NAME,
        new SpotifySharedTrack(this.configuration(), controller)
    );
  }

  public void initializeSpotifyAndResetDelay() {
    this.setReconnectDelay(ReconnectDelay.DEFAULT);
    this.initializeSpotifyAndResetDelay(true);
  }

  public void initializeSpotifyAndResetDelay(boolean force) {
    if (this.spotifyAPI.isInitialized()) {
      if (!force) {
        return;
      }
      this.spotifyAPI.stop();
    }

    if (!this.configuration().enabled().get()) {
      return;
    }

    this.spotifyAPI.initializeAsync(
        new de.labystudio.spotifyapi.config.SpotifyConfiguration.Builder()
            .autoReconnect(false)
            .exceptionReconnectDelay(this.reconnectDelay.getDelay())
            .nativesDirectory(Files.NATIVES.resolve("spotify"))
            .build()
    );
  }

  public void disconnect() {
    if (this.spotifyAPI.isInitialized()) {
      this.spotifyAPI.stop();
    }
  }

  public void setReconnectDelay(ReconnectDelay reconnectDelay) {
    this.reconnectDelay = reconnectDelay;
  }

  public void bumpReconnectDelay() {
    this.reconnectDelay = this.reconnectDelay.next();
  }

  public SpotifyAPI getSpotifyAPI() {
    return this.spotifyAPI;
  }

  @Override
  protected Class<SpotifyConfiguration> configurationClass() {
    return SpotifyConfiguration.class;
  }

  public static SpotifyAddon get() {
    return SpotifyAddon.instance;
  }

}
