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
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;

public class SpotifyTrackHudWidget extends TextHudWidget<TextHudWidgetConfig> implements
    SpotifyListener {
  private TextLine trackLine;
  private TextLine artistLine;

  private final Icon hudIcon = Icon.texture(
      ResourceLocation.create("spotify", "themes/vanilla/textures/settings/hud/spotify.png")).resolution(64,64);

  private final SpotifyAPI spotifyAPI = SpotifyAPIFactory.create();

  public SpotifyTrackHudWidget(String id) {
    super(id);
  }

  @Override
  public void load(TextHudWidgetConfig config) {
    super.load(config);
    this.trackLine = super.createLine("Track", "Loading...");
    this.artistLine = super.createLine("Artist", "Loading...");

    this.spotifyAPI.registerListener(this);
    this.spotifyAPI.initializeAsync();

    this.setIcon(hudIcon);
  }

  @Override
  public boolean isVisibleInGame() {
    return spotifyAPI.isConnected() && spotifyAPI.isPlaying();
  }

  @Override
  public void onConnect() {
    this.onPlayBackChanged(this.spotifyAPI.isPlaying());
  }

  @Override
  public void onTrackChanged(Track track) {
    if (track == null) {
      this.onPlayBackChanged(false);
    } else {
      this.trackLine.update(track.getName());
      this.artistLine.update(track.getArtist());
    }

    this.flushAll();
  }

  @Override
  public void onPositionChanged(int position) {

  }

  @Override
  public void onPlayBackChanged(boolean isPlaying) {
    if (!isPlaying) {
      this.trackLine.update("Not playing");
      this.artistLine.update("Not playing");
    } else {
      this.onTrackChanged(this.spotifyAPI.getTrack());
    }
  }

  @Override
  public void onSync() {
  }

  @Override
  public void onDisconnect(Exception exception) {

  }
}
