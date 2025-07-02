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

package net.labymod.addons.spotify.core.labymod.hudwidgets;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import net.labymod.addons.spotify.core.events.SpotifyConnectEvent;
import net.labymod.addons.spotify.core.events.SpotifyPlaybackChangedEvent;
import net.labymod.addons.spotify.core.events.SpotifyTrackChangedEvent;
import net.labymod.addons.spotify.core.labymod.hudwidgets.SpotifyHudWidget.SpotifyHudWidgetConfig;
import net.labymod.addons.spotify.core.labymod.hudwidgets.elements.widgets.SpotifyWidget;
import net.labymod.api.client.gui.hud.hudwidget.HudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.widget.WidgetHudWidget;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.screen.widget.widgets.hud.HudWidgetWidget;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.event.Subscribe;
import net.labymod.api.util.ThreadSafe;
import net.labymod.api.util.bounds.area.RectangleAreaPosition;

public class SpotifyHudWidget extends WidgetHudWidget<SpotifyHudWidgetConfig> {

  public static final String TRACK_CHANGE_REASON = "track_change";
  public static final String PLAYBACK_CHANGE_REASON = "playback_change";
  public static final String COVER_VISIBILITY_REASON = "cover_visibility";
  public static final String CONNECT_REASON = "connect";

  private final OpenSpotifyAPI openSpotifyAPI;
  private final SpotifyAPI spotifyAPI;
  private final Icon hudWidgetIcon;

  public SpotifyHudWidget(
      String id,
      Icon icon,
      OpenSpotifyAPI openSpotifyAPI,
      SpotifyAPI spotifyAPI
  ) {
    super(id, SpotifyHudWidgetConfig.class);

    this.hudWidgetIcon = icon;
    this.openSpotifyAPI = openSpotifyAPI;
    this.spotifyAPI = spotifyAPI;
  }

  @Override
  public void initializePreConfigured(SpotifyHudWidgetConfig config) {
    super.initializePreConfigured(config);

    config.setEnabled(true);
    config.setAreaIdentifier(RectangleAreaPosition.TOP_RIGHT);
    config.setX(-2);
    config.setY(2);
    config.setParentToTailOfChainIn(RectangleAreaPosition.TOP_RIGHT);
  }

  @Override
  public void load(SpotifyHudWidgetConfig config) {
    super.load(config);

    this.setIcon(this.hudWidgetIcon);

    config.showCover.addChangeListener(
        (property, oldValue, newValue) -> ThreadSafe.executeOnRenderThread(
            () -> this.requestUpdate(COVER_VISIBILITY_REASON))
    );
  }

  @Override
  public void initialize(HudWidgetWidget widget) {
    super.initialize(widget);

    boolean editorContext = widget.accessor().isEditor();

    SpotifyWidget spotifyWidget = new SpotifyWidget(this.openSpotifyAPI, this, editorContext);
    widget.addChild(spotifyWidget);
    widget.addId("spotify");
  }

  @Override
  public boolean isVisibleInGame() {
    return this.spotifyAPI.isConnected() && this.spotifyAPI.hasTrack();
  }

  @Subscribe
  public void onSpotifyConnectEvent(SpotifyConnectEvent event) {
    ThreadSafe.executeOnRenderThread(() -> {
      if (!this.isEnabled()) {
        return;
      }

      this.requestUpdate(CONNECT_REASON);
    });
  }

  @Subscribe
  public void onSpotifyTrackChangedEvent(SpotifyTrackChangedEvent event) {
    ThreadSafe.executeOnRenderThread(() -> {
      if (!this.isEnabled()) {
        return;
      }

      this.requestUpdate(TRACK_CHANGE_REASON);
    });
  }

  @Subscribe
  public void onSpotifyPlayBackChangedEvent(SpotifyPlaybackChangedEvent event) {
    ThreadSafe.executeOnRenderThread(() -> {
      if (!this.isEnabled()) {
        return;
      }

      this.requestUpdate(PLAYBACK_CHANGE_REASON);
    });
  }

  public SpotifyAPI spotifyAPI() {
    return this.spotifyAPI;
  }

  public static class SpotifyHudWidgetConfig extends HudWidgetConfig {

    @SwitchSetting
    private final ConfigProperty<Boolean> showCover = ConfigProperty.create(true);

    @SwitchSetting
    private final ConfigProperty<Boolean> minimizeIngame = ConfigProperty.create(true);

    public ConfigProperty<Boolean> showCover() {
      return this.showCover;
    }

    public ConfigProperty<Boolean> minimizeIngame() {
      return this.minimizeIngame;
    }
  }
}
