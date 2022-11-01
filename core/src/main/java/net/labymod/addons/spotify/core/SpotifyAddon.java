package net.labymod.addons.spotify.core;

import com.google.inject.Singleton;
import net.labymod.addons.spotify.core.hudwidgets.SpotifyTrackHudWidget;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonListener;

@Singleton
@AddonListener
public class SpotifyAddon extends LabyAddon<SpotifyConfiguration> {

  @Override
  protected void enable() {
    this.registerSettingCategory();

    this.labyAPI().hudWidgetRegistry().register(new SpotifyTrackHudWidget("spotify_track"));
  }

  @Override
  protected Class<SpotifyConfiguration> configurationClass() {
    return SpotifyConfiguration.class;
  }
}
