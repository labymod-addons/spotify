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

package net.labymod.addons.spotify.core;

import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.texture.ThemeTextureLocation;

import static net.labymod.api.client.gui.icon.Icon.sprite;
import static net.labymod.api.client.resources.texture.ThemeTextureLocation.of;

public class Textures {

  public static class SpriteControls {

    public static final ThemeTextureLocation TEXTURE = of("spotify:controls", 20, 20);

    public static final Icon PAUSE = sprite(TEXTURE, 0, 0, 10);
    public static final Icon PLAY = sprite(TEXTURE, 1, 0, 10);
    public static final Icon NEXT = sprite(TEXTURE, 0, 1, 10);
    public static final Icon PREVIOUS = sprite(TEXTURE, 1, 1, 10);

  }

}
