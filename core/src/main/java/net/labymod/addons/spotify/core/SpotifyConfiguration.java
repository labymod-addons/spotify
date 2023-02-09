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

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;

@SuppressWarnings("FieldMayBeFinal")
@ConfigName("settings")
public class SpotifyConfiguration extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true).addChangeListener(
      (property, prevValue, newValue) -> {
        if (newValue) {
          System.out.println("manual connect");
          SpotifyAddon.get().initializeSpotifyAPI();
        } else {
          System.out.println("manual disconnect");
          SpotifyAddon.get().disconnect();
        }
      });

  @SettingSection("sharing")
  @SwitchSetting
  private final ConfigProperty<Boolean> displayTracks = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> shareTracks = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> displayTrackCover = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> displayExplicitTracks = new ConfigProperty<>(false);

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Boolean> displayTracks() {
    return this.displayTracks;
  }

  public ConfigProperty<Boolean> shareTracks() {
    return this.shareTracks;
  }

  public ConfigProperty<Boolean> displayTrackCover() {
    return this.displayTrackCover;
  }

  public ConfigProperty<Boolean> displayExplicitTracks() {
    return this.displayExplicitTracks;
  }
}
