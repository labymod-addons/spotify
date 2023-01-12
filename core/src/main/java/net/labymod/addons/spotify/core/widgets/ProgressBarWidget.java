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

package net.labymod.addons.spotify.core.widgets;

import de.labystudio.spotifyapi.SpotifyAPI;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.util.ColorUtil;

@AutoWidget
public class ProgressBarWidget extends SimpleWidget {

  private final SpotifyAPI spotifyAPI;

  public ProgressBarWidget(SpotifyAPI api) {
    this.spotifyAPI = api;
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
  }

  @Override
  public void renderWidget(Stack stack, MutableMouse mouse, float partialTicks) {
    super.renderWidget(stack, mouse, partialTicks);

    float progress = this.spotifyAPI.hasTrack() && this.spotifyAPI.hasPosition()
        ? 1.0F / this.spotifyAPI.getTrack().getLength() * (float) this.spotifyAPI.getPosition()
        : 0;

    Bounds bounds = this.bounds();
    this.labyAPI.renderPipeline()
        .rectangleRenderer()
        .renderRectangle(
            stack,
            bounds,
            ColorUtil.toValue(0x444444)
        );
    this.labyAPI.renderPipeline()
        .rectangleRenderer()
        .renderRectangle(
            stack,
            bounds.getLeft(),
            bounds.getTop(),
            bounds.getLeft() + bounds.getWidth() * progress,
            bounds.getBottom(),
            ColorUtil.toValue(0x00FF00)
        );
  }
}
