package net.labymod.addons.spotify.core.labymod.snapshot;

import net.labymod.api.client.entity.player.Player;
import net.labymod.api.laby3d.renderer.snapshot.Extras;
import net.labymod.api.laby3d.renderer.snapshot.LabySnapshotFactory;
import net.labymod.api.service.annotation.AutoService;

@AutoService(LabySnapshotFactory.class)
public class SpotifyUserSnapshotFactory extends LabySnapshotFactory<Player, SpotifyUserSnapshot> {

  public SpotifyUserSnapshotFactory() {
    super(SpotifyExtraKeys.SPOTIFY_USER);
  }

  @Override
  protected SpotifyUserSnapshot create(Player player, Extras extras) {
    return new SpotifyUserSnapshot(player, extras);
  }
}
