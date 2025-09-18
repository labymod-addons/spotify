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

package net.labymod.addons.spotify.core.labymod.nametag;

import net.labymod.addons.spotify.core.SpotifyConfiguration;
import net.labymod.addons.spotify.core.sharing.SharedTrack;
import net.labymod.addons.spotify.core.sharing.TrackSharingController;
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

public class SpotifySharedTrack extends NameTag {

  private final RenderPipeline renderPipeline;
  private final RectangleRenderer rectangleRenderer;

  private final TrackSharingController controller;

  private boolean enabled;
  private boolean displayTracks;
  private boolean displayExplicitTracks;
  private boolean displayTrackCover;

  public SpotifySharedTrack(
      SpotifyConfiguration configuration,
      TrackSharingController controller
  ) {
    this.controller = controller;
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

    SharedTrack track = this.controller.getTrackOf(this.entity.getUniqueId());
    if (track == null) {
      return null;
    }

    if (track.isExplicit() && !this.displayExplicitTracks) {
      return null;
    }

    Component component = track.getComponent();
    if (component == null) {
      return null;
    }

    HorizontalAlignment alignment = track.getIcon() == null || !this.displayTrackCover
        ? HorizontalAlignment.CENTER
        : HorizontalAlignment.LEFT;
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

    stack.push();
    stack.translate(0, 0, -0.03F);
    this.rectangleRenderer.renderRectangle(
        stack,
        x,
        y,
        width,
        height,
        backgroundColor
    );
    stack.pop();

    float textX = x;

    SharedTrack track = this.controller.getTrackOf(this.entity.getUniqueId());
    if (track != null && this.displayTrackCover) {
      Icon icon = track.getIcon();

      if (icon != null) {
        this.renderPipeline.renderSeeThrough(this.entity, () -> {
          float size = height - 2;
          float iconX = x + 1;
          float iconY = y + 1;
          float progressY = iconY + size - 1;

          // Render the track cover icon
          icon.render(stack, iconX, iconY, size);

          // Render the progress bar
          double progress = track.getDaemonProgress();
          if (progress > 0) {
            int progressHeight = 1;
            stack.push();
            stack.translate(0, 0, 0.003F);
            this.rectangleRenderer.renderRectangle(
                stack,
                iconX,
                progressY,
                iconX + size,
                progressY + progressHeight,
                0xFF333333
            );
            stack.translate(0, 0, 0.003F);
            this.rectangleRenderer.renderRectangle(
                stack,
                iconX,
                progressY,
                (float) (iconX + size * progress),
                progressY + progressHeight,
                0xFF00FF00
            );
            stack.pop();
          }
        });

        textX += height + 1;
      }
    }

    super.renderText(stack, component, discrete, textColor, 0, textX, y + 1);
  }

  @Override
  public float getScale() {
    return 0.5F;
  }

  @Override
  public float getWidth() {
    return super.getWidth() + (this.displayTrackCover ? this.getHeight() : 0);
  }

  @Override
  public float getHeight() {
    return super.getHeight() + 1;
  }
}
