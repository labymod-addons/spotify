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

package net.labymod.addons.spotify.core.events;

import de.labystudio.spotifyapi.model.Track;
import net.labymod.api.event.Event;

public class SpotifyPlaybackChangedEvent implements Event {

  private final Track track;
  private final boolean isPlaying;

  public SpotifyPlaybackChangedEvent(Track track, boolean isPlaying) {
    this.track = track;
    this.isPlaying = isPlaying;
  }

  public Track getTrack() {
    return this.track;
  }

  public boolean isPlaying() {
    return this.isPlaying;
  }

}
