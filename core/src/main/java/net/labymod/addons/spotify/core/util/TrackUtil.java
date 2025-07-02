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

import de.labystudio.spotifyapi.open.model.track.Image;
import de.labystudio.spotifyapi.open.model.track.OpenTrack;

public class TrackUtil {

  public static Image getSmallestImage(OpenTrack openTrack) {
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
