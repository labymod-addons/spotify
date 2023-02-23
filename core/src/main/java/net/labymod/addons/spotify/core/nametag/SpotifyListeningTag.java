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

package net.labymod.addons.spotify.core.nametag;

import net.labymod.addons.spotify.core.SpotifyConfiguration;
import net.labymod.addons.spotify.core.misc.BroadcastController;
import net.labymod.addons.spotify.core.misc.BroadcastController.ReceivedBroadcast;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.entity.player.tag.tags.NameTag;
import net.labymod.api.client.gui.HorizontalAlignment;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import org.jetbrains.annotations.Nullable;

public class SpotifyListeningTag extends NameTag {

  private final SpotifyConfiguration configuration;
  private final BroadcastController broadcastController;

  private ReceivedBroadcast receivedBroadcast;

  public SpotifyListeningTag(SpotifyConfiguration configuration,
      BroadcastController broadcastController) {
    this.configuration = configuration;
    this.broadcastController = broadcastController;
  }

  @Override
  protected @Nullable RenderableComponent getRenderableComponent() {
    if (!(this.entity instanceof Player)) {
      return null;
    }

    SpotifyConfiguration configuration = this.configuration;
    if (!configuration.enabled().get() || !configuration.displayTracks().get()) {
      return null;
    }

    ReceivedBroadcast receivedBroadcast = this.broadcastController.get(this.entity.getUniqueId());
    if (receivedBroadcast == null) {
      return null;
    }

    if (receivedBroadcast.track != null && receivedBroadcast.track.explicit
        && !configuration.displayExplicitTracks().get()) {
      return null;
    }

    HorizontalAlignment alignment =
        receivedBroadcast.icon == null ? HorizontalAlignment.CENTER : HorizontalAlignment.LEFT;
    if (!configuration.displayTrackCover().get()) {
      alignment = HorizontalAlignment.CENTER;
    }

    this.receivedBroadcast = receivedBroadcast;

    //this shouldn't be happening as BroadcastController#get checks for this. But as all track information is being loaded async, it can still happen.
    Component component = receivedBroadcast.component;
    if (component == null) {
      return null;
    }

    return RenderableComponent.of(component, alignment);
  }

  @Override
  protected void renderText(
      Stack stack,
      RenderableComponent component,
      boolean discrete,
      int textColor,
      float x,
      float y
  ) {
    float height = this.getHeight();
    float textX = x;
    if (this.receivedBroadcast.icon != null && this.configuration.displayTrackCover().get()) {
      Laby.labyAPI().renderPipeline().renderSeeThrough(this.entity, () ->
          this.receivedBroadcast.icon.render(stack, x - 1, y + 0.5F, height - 2)
      );

      textX += height;
    }

    super.renderText(stack, component, discrete, textColor, textX, y);
  }

  @Override
  public float getScale() {
    return 0.5F;
  }

  @Override
  public float getWidth() {
    if (this.receivedBroadcast.icon == null || !this.configuration.displayTrackCover().get()) {
      return super.getWidth();
    }

    return super.getWidth() + this.getHeight();
  }
}
