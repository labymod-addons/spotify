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
import net.labymod.addons.spotify.core.labymod.snapshot.SpotifyExtraKeys;
import net.labymod.addons.spotify.core.labymod.snapshot.SpotifyUserSnapshot;
import net.labymod.addons.spotify.core.sharing.SharedTrack;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.entity.player.tag.tags.ComponentNameTag;
import net.labymod.api.client.gui.HorizontalAlignment;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.render.matrix.Stack;
import net.labymod.api.client.render.state.entity.AvatarSnapshot;
import net.labymod.api.client.render.state.entity.EntitySnapshot;
import net.labymod.api.laby3d.pipeline.RenderStates;
import net.labymod.api.laby3d.render.queue.CustomGeometryRenderer;
import net.labymod.api.laby3d.render.queue.SubmissionCollector;
import net.labymod.api.laby3d.render.queue.submissions.IconSubmission.DisplayMode;
import net.labymod.laby3d.api.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import java.util.List;

public class SpotifySharedTrack extends ComponentNameTag {

  private static final float BACKGROUND_DEPTH = -0.003F;
  private static final float PROGRESS_BACKGROUND_BAR_DEPTH = 0.003F;
  private static final float PROGRESS_PROGRESS_BAR_DEPTH = 0.004F;

  private static final int PROGRESS_BAR_BACKGROUND_COLOR = 0xFF333333;
  private static final int PROGRESS_BAR_COLOR = 0xFF00FF00;

  private Icon icon;
  private double progress;
  private HorizontalAlignment alignment = HorizontalAlignment.CENTER;

  private boolean enabled;
  private boolean displayTracks;
  private boolean displayExplicitTracks;
  private boolean displayTrackCover;

  public SpotifySharedTrack(SpotifyConfiguration configuration) {
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
  protected @NotNull List<Component> buildComponents(EntitySnapshot snapshot) {
    if (!(this.snapshot instanceof AvatarSnapshot avatar) || avatar.isDiscrete()) {
      return super.buildComponents(snapshot);
    }

    if (!this.enabled || !this.displayTracks) {
      return super.buildComponents(snapshot);
    }

    if (!this.snapshot.has(SpotifyExtraKeys.SPOTIFY_USER)) {
      return super.buildComponents(snapshot);
    }

    SpotifyUserSnapshot spotifyUser = this.snapshot.get(SpotifyExtraKeys.SPOTIFY_USER);

    SharedTrack track = spotifyUser.getTrack();
    if (track == null) {
      return super.buildComponents(snapshot);
    }

    if (track.isExplicit() && !this.displayExplicitTracks) {
      return super.buildComponents(snapshot);
    }

    this.icon = track.getIcon();
    this.alignment = this.icon == null || !this.displayTrackCover
        ? HorizontalAlignment.CENTER
        : HorizontalAlignment.LEFT;
    this.progress = track.getDaemonProgress();
    return track.getComponents();
  }

  @Override
  public void render(
      Stack stack,
      SubmissionCollector submissionCollector,
      EntitySnapshot snapshot
  ) {
    float size = this.getHeight();
    float backgroundWidth = this.getWidth();

    int backgroundArgb = Laby.labyAPI()
        .minecraft()
        .options()
        .getBackgroundColorWithOpacity(DEFAULT_BACKGROUND_COLOR);
    submissionCollector.submitCustomGeometry(
        stack,
        RenderStates.GUI,
        new ColoredRectangle(
            -1.0F, -1.0F,
            backgroundWidth + 1.0F, size + 1.0F,
            BACKGROUND_DEPTH,
            backgroundArgb
        )
    );

    super.render(stack, submissionCollector, snapshot);
    if (this.icon != null) {
      submissionCollector.submitIcon(
          stack,
          this.icon,
          DisplayMode.NORMAL,
          0, 0,
          size, size,
          -1
      );

      if (this.progress > 0) {
        float progressBarTop = size - 1.0F;
        submissionCollector.submitCustomGeometry(
            stack,
            RenderStates.GUI,
            new ColoredRectangle(
                0.0F, progressBarTop, size, size,
                PROGRESS_BACKGROUND_BAR_DEPTH,
                PROGRESS_BAR_BACKGROUND_COLOR
            )
        );

        submissionCollector.submitCustomGeometry(
            stack,
            RenderStates.GUI,
            new ColoredRectangle(
                0.0F, progressBarTop, (float) (size * this.progress), size,
                PROGRESS_PROGRESS_BAR_DEPTH,
                PROGRESS_BAR_COLOR
            )
        );
      }
    }
  }

  @Override
  protected void submitText(
      Stack stack,
      SubmissionCollector submissionCollector,
      EntitySnapshot snapshot,
      Component component,
      float xOffset, float yOffset
  ) {
    if (this.alignment == HorizontalAlignment.LEFT) {
      xOffset = this.getHeight() + 1.0F;
    } else if (this.alignment == HorizontalAlignment.CENTER) {
      xOffset = (this.getWidth() - this.fontRenderer.getWidth(component)) / 2.0F;
    }

    super.submitText(stack, submissionCollector, snapshot, component, xOffset, yOffset);
  }

  @Override
  protected int getBackgroundColor(EntitySnapshot snapshot) {
    return 0;
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
    return super.getHeight();
  }


  static class ColoredRectangle implements CustomGeometryRenderer {

    private final float left;
    private final float top;
    private final float right;
    private final float bottom;
    private final float depth;
    private final int argb;

    public ColoredRectangle(
        float left, float top, float right, float bottom,
        float depth,
        int argb
    ) {
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
      this.depth = depth;
      this.argb = argb;
    }

    @Override
    public void render(Matrix4f pose, VertexConsumer consumer) {
      consumer.addVertex(pose, this.left, this.top, this.depth).setBlankUv().setColor(this.argb);
      consumer.addVertex(pose, this.left, this.bottom, this.depth).setBlankUv().setColor(this.argb);
      consumer.addVertex(pose, this.right, this.bottom, this.depth).setBlankUv().setColor(this.argb);
      consumer.addVertex(pose, this.right, this.top, this.depth).setBlankUv().setColor(this.argb);
    }
  }
}
