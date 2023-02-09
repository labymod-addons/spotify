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
import net.labymod.api.Laby;
import net.labymod.api.client.gui.lss.property.LssProperty;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.mouse.MutableMouse;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.attributes.bounds.Bounds;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.matrix.Stack;

@AutoWidget
public class ProgressBarWidget extends SimpleWidget {

  private static final RectangleRenderer RECTANGLE_RENDERER = Laby.references().rectangleRenderer();

  private final SpotifyAPI spotifyAPI;

  private final LssProperty<Integer> foregroundColor = new LssProperty<>(0x00FF00);

  public ProgressBarWidget(SpotifyAPI api) {
    this.spotifyAPI = api;
  }

  @Override
  public void renderWidget(Stack stack, MutableMouse mouse, float partialTicks) {
    super.renderWidget(stack, mouse, partialTicks);
    float progress = 1.0F / this.spotifyAPI.getTrack().getLength() * this.spotifyAPI.getPosition();
    Bounds bounds = this.bounds();
    RECTANGLE_RENDERER
        .pos(bounds.getLeft(), bounds.getTop())
        .size(bounds.getWidth() * progress, bounds.getHeight())
        .color(this.foregroundColor.get())
        .render(stack);
  }

  public LssProperty<Integer> foregroundColor() {
    return this.foregroundColor;
  }
}
