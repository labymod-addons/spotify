package net.labymod.addons.spotify.core.secret;

import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import de.labystudio.spotifyapi.open.totp.model.Secret;
import de.labystudio.spotifyapi.open.totp.provider.SecretProvider;
import net.labymod.api.BuildData;
import net.labymod.api.util.io.web.request.Request;
import net.labymod.api.util.io.web.request.Response;

public class HttpSecretProvider implements SecretProvider {

  private static final String URL_SECRET_STORAGE = "https://laby.net/api/v3/spotify/totp-secret?format=1";

  private boolean updateRequired = false;

  @Override
  public Secret getSecret() {
    if (this.updateRequired) {
      throw new IllegalStateException(
          "The TOTP secret format is outdated. Please update the Spotify addon to the latest version."
      );
    }

    Response<Secret> response = Request.ofGson(Secret.class, OpenSpotifyAPI.GSON)
        .url(URL_SECRET_STORAGE)
        .userAgent(BuildData.getUserAgent())
        .executeSync();

    if (response.getStatusCode() == 426) {
      this.updateRequired = true;
      return this.getSecret();
    }

    if (response.isPresent()) {
      return response.get();
    }

    throw new IllegalStateException(
        "Failed to retrieve TOTP secret from " + URL_SECRET_STORAGE + "."
    );
  }
}
