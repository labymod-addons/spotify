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

package net.labymod.addons.spotify.core.hudwidgets;

import de.labystudio.spotifyapi.SpotifyAPI;
import net.labymod.addons.spotify.core.events.SpotifyConnectEvent;
import net.labymod.addons.spotify.core.events.SpotifyPlayBackChangedEvent;
import net.labymod.addons.spotify.core.events.SpotifyTrackChangedEvent;
import net.labymod.addons.spotify.core.hudwidgets.SpotifyHudWidget.SpotifyHudWidgetConfig;
import net.labymod.addons.spotify.core.widgets.SpotifyWidget;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.widget.WidgetHudWidget;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.AbstractWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSwitchable;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.gui.screen.ScreenOpenEvent;
import net.labymod.api.util.ThreadSafe;

@Link("spotify-widget.lss")
public class SpotifyHudWidget extends WidgetHudWidget<SpotifyHudWidgetConfig> {

  private final SpotifyAPI spotifyAPI;

  public SpotifyHudWidget(String id, SpotifyAPI spotifyAPI) {
    super(id, SpotifyHudWidgetConfig.class);

    this.spotifyAPI = spotifyAPI;
  }

  @Override
  public void load(SpotifyHudWidgetConfig config) {
    super.load(config);
  }

  @Override
  public void initialize(AbstractWidget<Widget> widget) {
    super.initialize(widget);

    SpotifyWidget spotifyWidget = new SpotifyWidget(this);
    widget.addChild(spotifyWidget);
    widget.addId("spotify");
  }

  @Override
  public boolean isVisibleInGame() {
    return this.spotifyAPI.isConnected() && this.spotifyAPI.hasTrack();
  }

  @Subscribe
  public void onScreenOpen(ScreenOpenEvent event) {
    if (event.getScreen() == null) {
      return;
    }
    System.out.println(event.getScreen());
    ThreadSafe.executeOnRenderThread(this::requestUpdate);
  }

  @Subscribe
  public void onSpotifyConnectEvent(SpotifyConnectEvent event) {
    ThreadSafe.executeOnRenderThread(this::requestUpdate);
  }

  @Subscribe
  public void onSpotifyTrackChangedEvent(SpotifyTrackChangedEvent event) {
    ThreadSafe.executeOnRenderThread(this::requestUpdate);
  }

  @Subscribe
  public void onSpotifyPlayBackChangedEvent(SpotifyPlayBackChangedEvent event) {
    ThreadSafe.executeOnRenderThread(this::requestUpdate);
  }

  public SpotifyAPI getSpotifyAPI() {
    return this.spotifyAPI;
  }

  public static class SpotifyHudWidgetConfig extends HudWidgetConfig {

    @SwitchSetting
    @SettingSwitchable(value = "showCover")
    private final ConfigProperty<Boolean> showCover = ConfigProperty.create(true);


    public ConfigProperty<Boolean> showCover() {
      return this.showCover;
    }

  }
}
