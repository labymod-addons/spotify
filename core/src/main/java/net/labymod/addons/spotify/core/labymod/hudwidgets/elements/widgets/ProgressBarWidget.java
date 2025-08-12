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

package net.labymod.addons.spotify.core.labymod.hudwidgets.elements.widgets;

import de.labystudio.spotifyapi.SpotifyAPI;
import net.labymod.api.client.gui.lss.property.LssProperty;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.ScreenContext;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.util.bounds.Rectangle;
import net.labymod.api.util.math.MathHelper;

@AutoWidget
public class ProgressBarWidget extends SimpleWidget {

  private final SpotifyAPI spotifyAPI;

  private final LssProperty<Integer> foregroundColor = new LssProperty<>(0x00FF00);

  public ProgressBarWidget(SpotifyAPI api) {
    this.spotifyAPI = api;
  }

  @Override
  public void renderWidget(ScreenContext context) {
    super.renderWidget(context);

    if (this.spotifyAPI.hasPosition()) {
      float progress =
          1.0F / this.spotifyAPI.getTrack().getLength() * this.spotifyAPI.getPosition();
      progress = MathHelper.clamp(progress, 0, 1);
      Bounds bounds = this.bounds();

      context.canvas().submitRect(
          Rectangle.relative(
              bounds.getLeft(), bounds.getTop(),
              bounds.getWidth() * progress, bounds.getHeight()
          ),
          this.foregroundColor.get()
      );
    }
  }

  public LssProperty<Integer> foregroundColor() {
    return this.foregroundColor;
  }
}
