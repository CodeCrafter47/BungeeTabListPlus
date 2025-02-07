/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.version;

import com.velocitypowered.api.proxy.Player;
import de.codecrafter47.data.velocity.VelocityClientVersionProvider;


public class VelocityProtocolVersionProvider implements ProtocolVersionProvider {

    private final VelocityClientVersionProvider clientVersionProvider = new VelocityClientVersionProvider();

    @Override
    public boolean has18OrLater(Player player) {
        return player.getProtocolVersion().getProtocol() >= 47;
    }

    @Override
    public boolean has113OrLater(Player player) {
        return player.getProtocolVersion().getProtocol() >= 393;
    }

    @Override
    public boolean has119OrLater(Player player) {
        return player.getProtocolVersion().getProtocol() >= 759;
    }

    @Override
    public boolean is18(Player player) {
        return player.getProtocolVersion().getProtocol() == 47;
    }

    @Override
    public String getVersion(Player player) {
        return clientVersionProvider.apply(player);
    }

    @Override
    public boolean has1193OrLater(Player player) {
        return player.getProtocolVersion().getProtocol() >= 761;
    }
    @Override
    public boolean has1203OrLater(Player player) {
        return player.getProtocolVersion().getProtocol() >= 765;
    }

}
