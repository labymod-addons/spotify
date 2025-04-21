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

import de.labystudio.spotifyapi.open.model.track.OpenTrack;
import net.labymod.addons.spotify.core.SpotifyConfiguration;
import net.labymod.addons.spotify.core.misc.BroadcastController;
import net.labymod.addons.spotify.core.misc.BroadcastController.ReceivedBroadcast;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.client.entity.player.tag.tags.NameTag;
import net.labymod.api.client.gui.HorizontalAlignment;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.RenderPipeline;
import net.labymod.api.client.render.draw.RectangleRenderer;
import net.labymod.api.client.render.font.RenderableComponent;
import net.labymod.api.client.render.matrix.Stack;
import org.jetbrains.annotations.Nullable;

public class SpotifyListeningTag extends NameTag {

  private final RenderPipeline renderPipeline;
  private final RectangleRenderer rectangleRenderer;

  private final BroadcastController broadcastController;

  private boolean enabled;
  private boolean displayTracks;
  private boolean displayExplicitTracks;
  private boolean displayTrackCover;

  private ReceivedBroadcast receivedBroadcast;

  public SpotifyListeningTag(
      SpotifyConfiguration configuration,
      BroadcastController broadcastController
  ) {
    this.broadcastController = broadcastController;
    this.renderPipeline = Laby.references().renderPipeline();
    this.rectangleRenderer = this.renderPipeline.rectangleRenderer();

    this.enabled = configuration.getAndAddListener(
        configuration.enabled(),
        value -> this.enabled = value
    );

    this.displayTracks = configuration.getAndAddListener(
        configuration.displayTracks(),
        value -> this.displayTracks = value
    );

    this.displayExplicitTracks = configuration.getAndAddListener(
        configuration.displayExplicitTracks(),
        value -> this.displayExplicitTracks = value
    );

    this.displayTrackCover = configuration.getAndAddListener(
        configuration.displayTrackCover(),
        value -> this.displayTrackCover = value
    );
  }

  @Override
  protected @Nullable RenderableComponent getRenderableComponent() {
    if (!(this.entity instanceof Player) || this.entity.isCrouching()) {
      return null;
    }

    if (!this.enabled || !this.displayTracks) {
      return null;
    }

    ReceivedBroadcast receivedBroadcast = this.broadcastController.get(this.entity.getUniqueId());
    if (receivedBroadcast == null) {
      return null;
    }

    OpenTrack track = receivedBroadcast.track;
    if (track != null && track.explicit && !this.displayExplicitTracks) {
      return null;
    }

    HorizontalAlignment alignment;
    if (receivedBroadcast.icon == null || !this.displayTrackCover) {
      alignment = HorizontalAlignment.CENTER;
    } else {
      alignment = HorizontalAlignment.LEFT;
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
      int backgroundColor,
      float x,
      float y
  ) {
    float width = this.getWidth();
    float height = this.getHeight();
    this.rectangleRenderer.renderRectangle(
        stack,
        x,
        y,
        width,
        height,
        backgroundColor
    );

    float textX = x;

    Icon icon = this.receivedBroadcast == null ? null : this.receivedBroadcast.icon;
    if (icon != null && this.displayTrackCover) {
      this.renderPipeline.renderSeeThrough(this.entity, () -> {
        icon.render(stack, x + 1, y + 1, height - 2);
      });

      textX += height + 1;
    }

    super.renderText(stack, component, discrete, textColor, 0, textX, y + 1);
  }

  @Override
  public float getScale() {
    return 0.5F;
  }

  @Override
  public float getWidth() {
    if (this.receivedBroadcast.icon == null || !this.displayTrackCover) {
      return super.getWidth();
    }

    return super.getWidth() + this.getHeight();
  }

  @Override
  public float getHeight() {
    return super.getHeight() + 1;
  }
}
