package net.labymod.addons.spotify.core.hudwidgets;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidget;
import net.labymod.api.client.gui.hud.hudwidget.text.TextHudWidgetConfig;
import net.labymod.api.client.gui.hud.hudwidget.text.TextLine;


public class SpotifyTrackHudWidget extends TextHudWidget<TextHudWidgetConfig> implements
    SpotifyListener {
  private TextLine trackLine;
  private TextLine artistLine;

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
