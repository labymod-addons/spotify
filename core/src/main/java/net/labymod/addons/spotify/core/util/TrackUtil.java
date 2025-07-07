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

package net.labymod.addons.spotify.core.util;

import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import de.labystudio.spotifyapi.open.model.track.Image;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import net.labymod.addons.spotify.core.Textures;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.CompletableResourceLocation;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.client.resources.texture.GameImage;
import net.labymod.api.client.resources.texture.SimpleTexture;
import net.labymod.api.client.resources.texture.TextureDetails;
import net.labymod.api.client.resources.texture.TextureRepository;
import net.labymod.api.client.resources.texture.TextureRepository.TextureRegistrationCallback;

public class TrackUtil {

  private static final Cache<Icon> ICON_CACHE = new Cache<>(100, icon -> {
    ResourceLocation resourceLocation = icon.getResourceLocation();
    if (Objects.equals(resourceLocation, Textures.ICON_TEXTURE)) {
      return; // Don't release the default icon texture
    }

    Laby.references()
        .textureRepository()
        .queueTextureRelease(resourceLocation);
  });

  public static synchronized Icon createIcon(
      OpenSpotifyAPI api,
      Track track
  ) {
    Icon cachedIcon = ICON_CACHE.get(track.getId());
    if (cachedIcon != null) {
      return cachedIcon;
    }

    CompletableResourceLocation completable = new CompletableResourceLocation(
        Textures.ICON_TEXTURE
    );
    ResourceLocation resourceLocation = getResourceLocationForTrackId(track.getId());

    BufferedImage coverArt = track.getCoverArt();
    if (coverArt == null) {
      api.requestOpenTrackAsync(track.getId(), openTrack -> {
        registerOpenTrackImage(
            openTrack,
            resourceLocation,
            completable::executeCompletableListeners
        );
      });
    } else {
      GameImage gameImage = Laby.references()
          .gameImageProvider()
          .getImage(coverArt);
      SimpleTexture texture = new SimpleTexture(resourceLocation, gameImage);
      texture.bindTo(new TextureRegistrationCallback() {
        @Override
        public void onBeforeTextureRegistration() {
          texture.upload();
        }

        @Override
        public void onAfterTextureRegistration() {
          completable.executeCompletableListeners(resourceLocation);
        }
      });
    }

    Icon icon = Icon.completable(completable);
    ICON_CACHE.push(track.getId(), icon);
    return icon;
  }

  private static ResourceLocation getResourceLocationForTrackId(String trackId) {
    return Laby.references()
        .resources()
        .resourceLocationFactory()
        .create(
            "spotify",
            "track/" + trackId.toLowerCase(Locale.ENGLISH)
        );
  }

  public static synchronized Icon createIcon(OpenTrack track) {
    Icon cachedIcon = ICON_CACHE.get(track.id);
    if (cachedIcon != null) {
      return cachedIcon;
    }

    CompletableResourceLocation completable = new CompletableResourceLocation(
        Textures.ICON_TEXTURE
    );
    ResourceLocation resource = getResourceLocationForTrackId(track.id);

    registerOpenTrackImage(
        track,
        resource,
        completable::executeCompletableListeners
    );

    Icon icon = Icon.completable(completable);
    ICON_CACHE.push(track.id, icon);
    return icon;
  }

  private static synchronized void registerOpenTrackImage(
      OpenTrack openTrack,
      ResourceLocation resourceLocation,
      Consumer<ResourceLocation> callback
  ) {
    Image artwork = getSmallestImage(openTrack);
    if (artwork == null) {
      return; // Note: We don't have to update because it's already using the fallback
    }

    TextureDetails details = TextureDetails.builder(resourceLocation)
        .withUrl(artwork.url)
        .withFinishHandler(texture -> {
          callback.accept(resourceLocation);
        })
        .build();
    TextureRepository textureRepository = Laby.references().textureRepository();
    textureRepository.getOrRegisterTexture(details);
  }

  public static Component getShortTrackNameAndArtist(OpenTrack openTrack) {
    String name = openTrack.name;
    int bracketIndex = name.indexOf("(");
    if (bracketIndex != -1 && name.indexOf("Remix", bracketIndex) == -1) {
      name = name.substring(0, bracketIndex);
    }

    if (name.length() > 32) {
      name = name.substring(0, 29) + "...";
    }

    String artist = openTrack.getArtists();
    if (artist.length() > 32) {
      artist = artist.substring(0, 29) + "...";
    }

    String finalName = name.trim();
    String finalArtist = artist.trim();

    return Component.text(finalName + "\n" + finalArtist);
  }

  public static Image getSmallestImage(OpenTrack openTrack) {
    if (openTrack == null || openTrack.album == null || openTrack.album.images == null) {
      return null; // No images available
    }

    Image target = null;
    for (Image image : openTrack.album.images) {
      if (target == null || image.width < target.width) {
        target = image;
      }
    }
    return target;
  }

  public static boolean isTrackIdValid(String trackId) {
    for (char c : trackId.toCharArray()) {
      boolean isValidCharacter = c >= 'a' && c <= 'z'
          || c >= 'A' && c <= 'Z'
          || c >= '0' && c <= '9';
      if (!isValidCharacter) {
        return false;
      }
    }
    return true;
  }

}
