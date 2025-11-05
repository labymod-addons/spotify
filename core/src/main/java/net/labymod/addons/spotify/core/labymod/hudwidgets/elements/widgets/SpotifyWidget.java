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
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import net.labymod.addons.spotify.core.SpotifyAddon;
import net.labymod.addons.spotify.core.Textures.SpriteControls;
import net.labymod.addons.spotify.core.labymod.hudwidgets.SpotifyHudWidget;
import net.labymod.addons.spotify.core.util.TrackUtil;
import net.labymod.api.Laby;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.hud.hudwidget.HudWidget.Updatable;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.activity.Link;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;
import org.jetbrains.annotations.Nullable;

@AutoWidget
@Link("spotify-widget.lss")
public class SpotifyWidget extends FlexibleContentWidget implements Updatable {

  private static final String PROGRESS_VISIBLE_KEY = "--progress-visible";
  private static final String LARGE_PROGRESS_VISIBLE_KEY = "--large-progress-visible";

  private final OpenSpotifyAPI openSpotifyAPI;
  private final SpotifyHudWidget hudWidget;
  private final SpotifyAPI spotifyAPI;
  private final boolean editorContext;
  private ComponentWidget trackWidget;
  private ComponentWidget artistWidget;
  private IconWidget coverWidget;
  private DivWidget controlsWidget;
  private ComponentWidget currentTimeWidget;
  private ComponentWidget totalTimeWidget;
  private IconWidget playPauseWidget;
  private int lastTickPosition = -1;

  public SpotifyWidget(
      OpenSpotifyAPI openSpotifyAPI,
      SpotifyHudWidget hudWidget,
      boolean editorContext
  ) {
    this.openSpotifyAPI = openSpotifyAPI;
    this.hudWidget = hudWidget;
    this.editorContext = editorContext;
    this.spotifyAPI = this.hudWidget.spotifyAPI();

    boolean hasTrack = this.spotifyAPI.hasTrack() && this.spotifyAPI.hasPosition();
    this.setVariable(PROGRESS_VISIBLE_KEY, hasTrack);
    this.setVariable(LARGE_PROGRESS_VISIBLE_KEY, hasTrack);

    this.setPressable(() -> {
      // Retry
      if (!this.spotifyAPI.isConnected()) {
        this.artistWidget.setVisible(false);
        this.hudWidget.addon().initializeSpotifyAndResetDelay();
      }
    });
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);
    this.children.clear();
    boolean maximize = this.editorContext || !this.hudWidget.getConfig().minimizeIngame().get();
    if (maximize) {
      this.addId("maximized");
    }

    if (!this.hudWidget.getConfig().showCover().get()) {
      this.addId("no-cover");
    }

    boolean leftAligned = this.hudWidget.anchor().isLeft();
    this.addId(leftAligned ? "left" : "right");

    this.coverWidget = new IconWidget(this.hudWidget.getIcon());
    this.coverWidget.addId("cover");

    if (!maximize) {
      ProgressBarWidget minimizedProgressBar = new ProgressBarWidget(this.spotifyAPI);
      minimizedProgressBar.addId("minimized-bar");
      this.coverWidget.addChild(minimizedProgressBar);
    }

    // add cover if the hud widget is left-aligned
    if (leftAligned) {
      this.addContent(this.coverWidget);
    }

    FlexibleContentWidget player = new FlexibleContentWidget();
    player.addId("player");

    FlexibleContentWidget textAndControl = new FlexibleContentWidget();
    textAndControl.addId("text-and-control");

    // Text
    VerticalListWidget<ComponentWidget> text = new VerticalListWidget<>();
    text.addId("text");

    this.trackWidget = ComponentWidget.empty();
    text.addChild(this.trackWidget);

    this.artistWidget = ComponentWidget.empty();
    text.addChild(this.artistWidget);

    // Controls
    this.controlsWidget = new DivWidget();
    this.controlsWidget.addId("controls");

    this.playPauseWidget = new IconWidget(
        this.spotifyAPI.isPlaying() ? SpriteControls.PAUSE : SpriteControls.PLAY
    );
    this.playPauseWidget.addId("play");
    this.playPauseWidget.setPressable(() -> {
      this.playPauseWidget.icon().set(
          this.spotifyAPI.isPlaying() ? SpriteControls.PLAY : SpriteControls.PAUSE
      );

      this.pressMediaKey(MediaKey.PLAY_PAUSE);
    });
    this.controlsWidget.addChild(this.playPauseWidget);

    IconWidget previousTrack = new IconWidget(SpriteControls.PREVIOUS);
    previousTrack.addId("previous");
    previousTrack.setPressable(() -> this.pressMediaKey(MediaKey.PREV));
    this.controlsWidget.addChild(previousTrack);

    IconWidget nextTrack = new IconWidget(SpriteControls.NEXT);
    nextTrack.addId("next");
    nextTrack.setPressable(() -> this.pressMediaKey(MediaKey.NEXT));
    this.controlsWidget.addChild(nextTrack);

    // Add text & controls to player based on the alignment
    if (leftAligned) {
      textAndControl.addFlexibleContent(text);
      textAndControl.addContent(this.controlsWidget);
    } else {
      textAndControl.addContent(this.controlsWidget);
      textAndControl.addFlexibleContent(text);
    }

    player.addFlexibleContent(textAndControl);

    // Add progress bar to player
    FlexibleContentWidget progress = new FlexibleContentWidget();
    progress.addId("progress");

    this.currentTimeWidget = ComponentWidget.empty();
    progress.addContent(this.currentTimeWidget);

    ProgressBarWidget progressBar = new ProgressBarWidget(this.spotifyAPI);
    progressBar.addId("full-bar");
    progress.addFlexibleContent(progressBar);

    this.totalTimeWidget = ComponentWidget.empty();
    progress.addContent(this.totalTimeWidget);

    // Add progress to player and player to this widget
    player.addContent(progress);
    this.addContent(player);

    // add cover if the hud widget is right-aligned
    if (!leftAligned) {
      this.addContent(this.coverWidget);
    }

    this.updateTrack(this.spotifyAPI.getTrack());
  }

  @Override
  public void tick() {
    super.tick();

    boolean hasTrack = this.spotifyAPI.hasTrack() && this.spotifyAPI.hasPosition();
    this.setVariable(PROGRESS_VISIBLE_KEY, hasTrack);

    // everything with the variable LARGE_PROGRESS_VISIBLE_KEY is an ugly hotfix for IDEA-16722. Revert the changes and you'll see
    if (!this.editorContext) {
      boolean isChatOpen = Laby.references().chatAccessor().isChatOpen();

      if (!this.hudWidget.getConfig().minimizeIngame().get() || isChatOpen) {
        this.addId("maximized");
        this.setVariable(LARGE_PROGRESS_VISIBLE_KEY, hasTrack);
      } else {
        this.removeId("maximized");
        this.setVariable(LARGE_PROGRESS_VISIBLE_KEY, false);
      }
    } else {
      this.setVariable(LARGE_PROGRESS_VISIBLE_KEY, hasTrack);
    }

    if (this.spotifyAPI.hasPosition() && this.currentTimeWidget != null) {
      int position = this.spotifyAPI.getPosition() / 1000;
      if (this.lastTickPosition < 0 || this.lastTickPosition != position) {
        String positionDisplay = String.format("%d:%02d", position / 60, position % 60);
        this.currentTimeWidget.setComponent(Component.text(positionDisplay));
        this.lastTickPosition = position;
      }
    }
  }

  @Override
  public void update(@Nullable String reason) {
    if (reason == null || reason.equals(SpotifyHudWidget.CONNECT_REASON)) {
      this.reInitialize();
      return;
    }

    if (reason.equals(SpotifyHudWidget.PLAYBACK_CHANGE_REASON) && this.playPauseWidget != null) {
      this.playPauseWidget.icon().set(
          this.spotifyAPI.isPlaying() ? SpriteControls.PAUSE : SpriteControls.PLAY
      );
    }

    if (reason.equals(SpotifyHudWidget.TRACK_CHANGE_REASON)) {
      this.updateTrack(this.spotifyAPI.getTrack());
    }

    if (reason.equals(SpotifyHudWidget.COVER_VISIBILITY_REASON)) {
      boolean showCover = this.hudWidget.getConfig().showCover().get();
      if (showCover) {
        this.removeId("no-cover");
      } else {
        this.addId("no-cover");
      }
    }
  }

  private void updateTrack(Track track) {
    if (this.trackWidget == null || this.artistWidget == null) {
      return;
    }

    this.trackWidget.setComponent(Component.text(track == null ? "Not playing" : track.getName()));
    this.artistWidget.setComponent(
        Component.text(track == null ? "Click to retry" : track.getArtist())
    );

    this.artistWidget.setVisible(true);

    if (track == null || track.getLength() <= 0 || !TrackUtil.isTrackIdValid(track.getId())) {
      this.controlsWidget.setVisible(false);
      return;
    }

    this.controlsWidget.setVisible(true);

    int length = track.getLength() / 1000;
    String totalTimeDisplay = String.format("%d:%02d", length / 60, length % 60);
    this.totalTimeWidget.setComponent(Component.text(totalTimeDisplay));

    Icon icon = TrackUtil.createIcon(this.openSpotifyAPI, track);
    this.coverWidget.icon().set(icon);
  }

  private void pressMediaKey(MediaKey mediaKey) {
    if (!this.spotifyAPI.isConnected()) {
      return;
    }

    try {
      this.spotifyAPI.pressMediaKey(mediaKey);
    } catch (IllegalArgumentException e) {
      SpotifyAddon.get().logger().error("Failed to press media key", e);
    }
  }
}
