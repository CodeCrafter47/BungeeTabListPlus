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

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.data.AbstractCompositeDataProvider;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.taboverlay.config.player.Player;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class HiddenPlayersManager extends AbstractCompositeDataProvider<Boolean> {

    private final List<VanishProvider> vanishProviders = new ArrayList<>();
    private boolean active = false;

    public HiddenPlayersManager() {
        super(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN);
    }

    public void enable() {
        active = true;
    }

    public void disable() {
        active = false;
    }

    /**
     * Adds a vanish provider. To be called during setup.
     */
    public void addVanishProvider(String name, DataKey<Boolean> dataIsHidden) {
        if (active) {
            throw new IllegalStateException("Cannot call addVanishProvider() after enable()");
        }
        this.vanishProviders.add(new VanishProvider(name, dataIsHidden));
    }

    @Override
    protected void registerListener(Player player, DataKey<Boolean> key, Runnable listener) {
        for (VanishProvider vanishProvider : vanishProviders) {
            player.addDataChangeListener(vanishProvider.dataIsHidden, listener);
        }
    }

    @Override
    protected void unregisterListener(Player player, DataKey<Boolean> key, Runnable listener) {
        for (VanishProvider vanishProvider : vanishProviders) {
            player.removeDataChangeListener(vanishProvider.dataIsHidden, listener);
        }
    }

    @Override
    protected Boolean computeCompositeData(BungeePlayer player, DataKey<Boolean> key) {
        boolean hidden = false;
        for (VanishProvider vanishProvider : vanishProviders) {
            hidden = hidden || Boolean.TRUE.equals(player.get(vanishProvider.dataIsHidden));
        }
        return hidden;
    }

    public List<String> getActiveVanishProviders(BungeePlayer player) {
        List<String> activeVanishProviders = new ArrayList<>();
        for (VanishProvider vanishProvider : vanishProviders) {
            if (Boolean.TRUE.equals(player.get(vanishProvider.dataIsHidden))) {
                activeVanishProviders.add(vanishProvider.name);
            }
        }
        return activeVanishProviders;
    }

    @Data
    private static class VanishProvider {
        private final String name;
        private final DataKey<Boolean> dataIsHidden;
    }
}
