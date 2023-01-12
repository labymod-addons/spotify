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
import de.labystudio.spotifyapi.model.MediaKey;
import de.labystudio.spotifyapi.model.Track;
import de.labystudio.spotifyapi.open.OpenSpotifyAPI;
import net.kyori.adventure.text.Component;
import net.labymod.addons.spotify.core.Textures.SpriteControls;
import net.labymod.addons.spotify.core.hudwidgets.SpotifyHudWidget;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.gui.lss.property.annotation.AutoWidget;
import net.labymod.api.client.gui.screen.Parent;
import net.labymod.api.client.gui.screen.widget.SimpleWidget;
import net.labymod.api.client.gui.screen.widget.Widget;
import net.labymod.api.client.gui.screen.widget.widgets.ComponentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.DivWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.FlexibleContentWidget;
import net.labymod.api.client.gui.screen.widget.widgets.layout.list.VerticalListWidget;
import net.labymod.api.client.gui.screen.widget.widgets.renderer.IconWidget;

@AutoWidget
public class SpotifyWidget extends SimpleWidget {
  private static final OpenSpotifyAPI openSpotifyAPI = new OpenSpotifyAPI();
  private final SpotifyHudWidget hudWidget;
  private final SpotifyAPI spotifyAPI;
  private ComponentWidget trackWidget;
  private ComponentWidget artistWidget;
  private IconWidget iconWidget;
  private ComponentWidget currentTimeWidget;
  private ComponentWidget totalTimeWidget;
  private IconWidget playButton;

  public SpotifyWidget(SpotifyHudWidget hudWidget) {
    this.hudWidget = hudWidget;
    this.spotifyAPI = this.hudWidget.getSpotifyAPI();
  }

  @Override
  public void initialize(Parent parent) {
    super.initialize(parent);

    this.iconWidget = new IconWidget(Icon.head("spotify"));
    this.iconWidget.addId("cover");

    this.trackWidget = ComponentWidget.text("Loading...");
    this.artistWidget = ComponentWidget.text("Loading...");

    // Player and Progress
    FlexibleContentWidget spotifyPlayerWidget = new FlexibleContentWidget();
    spotifyPlayerWidget.addId("spotify-player-widget");
    {
      // Icon
      boolean alignmentRight = this.hudWidget.anchor().isRight();
      if (!alignmentRight && this.hudWidget.getConfig().showCover().get()) {
        spotifyPlayerWidget.addContent(this.iconWidget);
      }

      // Player
      FlexibleContentWidget playerWrapper = new FlexibleContentWidget();
      playerWrapper.addId("player");
      {
        // Control and Text
        FlexibleContentWidget controlAndTextWrapper = new FlexibleContentWidget();
        controlAndTextWrapper.addId("control-and-text");
        {
          // Controls
          DivWidget controlsWrapper = new DivWidget();
          controlsWrapper.addId("controls");
          {
            this.playButton = new IconWidget(
                this.spotifyAPI.isPlaying() ? SpriteControls.PAUSE : SpriteControls.PLAY);
            this.playButton.addId("play");
            this.playButton.setPressable(() -> {
              this.spotifyAPI.pressMediaKey(MediaKey.PLAY_PAUSE);
              this.reInitialize();
            });
            controlsWrapper.addChild(this.playButton);

            IconWidget previousTrack = new IconWidget(SpriteControls.PREVIOUS);
            previousTrack.addId("previous");
            previousTrack.setPressable(() -> this.spotifyAPI.pressMediaKey(MediaKey.PREV));
            controlsWrapper.addChild(previousTrack);

            IconWidget nextTrack = new IconWidget(SpriteControls.NEXT);
            nextTrack.addId("next");
            nextTrack.setPressable(() -> this.spotifyAPI.pressMediaKey(MediaKey.NEXT));
            controlsWrapper.addChild(nextTrack);
          }
          if (alignmentRight) {
            controlAndTextWrapper.addContent(controlsWrapper);
          }

          // Text
          VerticalListWidget<Widget> textWrapper = new VerticalListWidget<>();
          textWrapper.addId("text");
          textWrapper.addId(alignmentRight ? "right" : "left");
          {
            textWrapper.addChild(this.trackWidget);
            textWrapper.addChild(this.artistWidget);
          }
          controlAndTextWrapper.addFlexibleContent(textWrapper);

          if (!alignmentRight) {
            controlAndTextWrapper.addContent(controlsWrapper);
          }
        }
        playerWrapper.addFlexibleContent(controlAndTextWrapper);

        // Progress
        FlexibleContentWidget progressWrapper = new FlexibleContentWidget();
        progressWrapper.addId("progress");
        {
          // Current Time
          this.currentTimeWidget = ComponentWidget.text("0:00");
          progressWrapper.addContent(this.currentTimeWidget);

          // Progress bar
          DivWidget barWrapperWidget = new DivWidget();
          barWrapperWidget.addId("bar-wrapper");
          {
            ProgressBarWidget progressBar = new ProgressBarWidget(this.spotifyAPI);
            progressBar.addId("bar");
            barWrapperWidget.addChild(progressBar);
          }
          progressWrapper.addFlexibleContent(barWrapperWidget);

          // Length
          this.totalTimeWidget = ComponentWidget.text("0:00");
          progressWrapper.addContent(this.totalTimeWidget);
        }
        playerWrapper.addContent(progressWrapper);
      }
      spotifyPlayerWidget.addFlexibleContent(playerWrapper);

      // Icon
      if (alignmentRight && this.hudWidget.getConfig().showCover().get()) {
        spotifyPlayerWidget.addContent(this.iconWidget);
      }
    }

//    if (this.labyAPI.minecraft().minecraftWindow().currentScreen()) {
//      spotifyPlayerWidget.addId("minimized");
//    }

    this.addChild(spotifyPlayerWidget);

    this.updateTrack(this.spotifyAPI.getTrack());
  }

  @Override
  public void tick() {
    super.tick();

    if (this.spotifyAPI.hasPosition() && this.currentTimeWidget != null) {
      int position = this.spotifyAPI.getPosition() / 1000;
      String positionDisplay = String.format("%d:%02d", position / 60, position % 60);
      this.currentTimeWidget.setComponent(Component.text(positionDisplay));
    }
  }

  private void updateTrack(Track track) {
    if (track == null || this.trackWidget == null || this.artistWidget == null) {
      return;
    }

    this.trackWidget.setComponent(Component.text(track.getName()));
    this.artistWidget.setComponent(Component.text(track.getArtist()));

    int length = track.getLength() / 1000;
    String totalTimeDisplay = String.format("%d:%02d", length / 60, length % 60);
    this.totalTimeWidget.setComponent(Component.text(totalTimeDisplay));

    Icon icon = Icon.head("spotify");
    try {
      String url = openSpotifyAPI.requestImageUrl(track);
      icon = Icon.url(url);
    } catch (Exception e) {
      e.printStackTrace();
    }

    this.iconWidget.icon().set(icon);
  }
}
