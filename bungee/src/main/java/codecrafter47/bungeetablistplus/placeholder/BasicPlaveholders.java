/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.PlaceholderProvider;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.bungeetablistplus.tablist.SlotBuilder;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import codecrafter47.data.Values;

import java.util.Optional;

public class BasicPlaveholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bindRegex("\\[PING=(-?\\d+)\\]").to((placeholderManager, matcher) -> SlotTemplate.ping(Integer.valueOf(matcher.group(1))));
        bindRegex("\\[SKIN=([^]]+)\\]").to((placeholderManager, matcher) -> new SlotTemplate() {
            SlotTemplate args = BungeeTabListPlus.getInstance().getPlaceholderManager().parseSlot(matcher.group(1));

            @Override
            public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                return builder.setSkin(BungeeTabListPlus.getInstance().getSkinManager().getSkin(args.buildSlot(context).getText()));
            }
        });
        bind("name").alias("player").to(context -> {
            Optional<String> displayName = BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(context.getPlayer(), Values.Player.Bukkit.DisplayName);
            if (displayName.isPresent()) {
                return displayName.get();
            }
            if (context.getPlayer() instanceof BungeePlayer)
                return ((BungeePlayer) context.getPlayer()).getPlayer().getDisplayName();
            return context.getPlayer().getName();
        });
        bind("rawname").to(context -> context.getPlayer().getName());
        bind("server").to(context -> BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().getServerAlias(context.getServer().get(0)));
        bind("permprefix").alias("prefix").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getPrefix(context.getPlayer()));
        bind("prefixColor").to(context -> {
            String prefix = BungeeTabListPlus.getInstance().getPermissionManager().getPrefix(context.getPlayer());
            if (prefix.length() < 2) {
                return "";
            }
            return prefix.substring(0, 2);
        });
        bind("permsuffix").alias("suffix").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getSuffix(context.getPlayer()));
        bind("displayprefix").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getDisplayPrefix(context.getPlayer()));
        bind("ping").to(context -> String.format("%d", context.getPlayer().getPing()));
        bind("group").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getMainGroup(context.getPlayer()));
        bind("uuid").to(context -> context.getPlayer().getUniqueID().toString());
        bind("internalServerName").to(context -> context.getServer().get(0));
        bind("serverPrefix").withArgs().to((context, args) -> {
            if (args != null && !args.isEmpty()) {
                return BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().serverPrefixes.get(args);
            } else {
                return BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().serverPrefixes.get(context.getServer().get(0));
            }
        });
        bind("other_count").to(context -> String.format("%d", context.getOtherPlayerCount()));
        bind("newline").to(context -> "\n");
    }
}
