package net.labymod.addons.spotify.core.hudwidgets;

import de.labystudio.spotifyapi.SpotifyAPI;
import de.labystudio.spotifyapi.SpotifyAPIFactory;
import de.labystudio.spotifyapi.SpotifyListener;
import de.labystudio.spotifyapi.model.Track;
import net.labymod.api.client.gui.hud2.text.NewTextHudWidget;
import net.labymod.api.client.gui.hud2.text.NewTextHudWidgetConfig;
import net.labymod.api.client.gui.hud2.text.NewTextLine;


public class SpotifyTrackHudWidget extends NewTextHudWidget<NewTextHudWidgetConfig> implements
    SpotifyListener {
  private NewTextLine trackLine;
  private NewTextLine artistLine;

  private final SpotifyAPI spotifyAPI = SpotifyAPIFactory.create();

  public SpotifyTrackHudWidget(String id) {
    super(id);
  }

  @Override
  public void load(NewTextHudWidgetConfig config) {
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
    if (spotifyAPI.isPlaying()) {
      this.onTrackChanged(spotifyAPI.getTrack());
    } else {
      this.onPlayBackChanged(false);
    }
  }

  @Override
  public void onTrackChanged(Track track) {
    this.trackLine.update(track.getName());
    this.artistLine.update(track.getArtist());
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
    }
  }

  @Override
  public void onSync() {
  }

  @Override
  public void onDisconnect(Exception exception) {

  }
}
