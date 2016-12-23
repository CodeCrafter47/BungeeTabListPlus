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
import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotBuilder;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.Player;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;

public class BasicPlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bindRegex("\\[PING=([^]]+)\\]").to((placeholderManager, matcher) -> {
            if (matcher.group(1).equals("?")) {
                BungeeTabListPlus.getInstance().requireUpdateInterval(0.25);
                return new SlotTemplate() {
                    @Override
                    public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                        int bars = (int) (((System.currentTimeMillis() / 250) % 8) - 3);
                        if (bars <= 0) {
                            bars = 2 - bars;
                        }
                        switch (bars) {
                            case 5:
                                return builder.setPing(0);
                            case 4:
                                return builder.setPing(150);
                            case 3:
                                return builder.setPing(300);
                            case 2:
                                return builder.setPing(600);
                            case 1:
                                return builder.setPing(1000);
                            default:
                                return builder.setPing(-1);
                        }
                    }
                };
            } else {
                return new SlotTemplate() {
                    SlotTemplate args = BungeeTabListPlus.getInstance().getPlaceholderManager0().parseSlot(matcher.group(1));

                    @Override
                    public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                        try {
                            return builder.setPing(Integer.valueOf(args.buildSlot(context).getText()));
                        } catch (NumberFormatException ex) {
                            return args.buildSlot(builder.appendText("[PING="), context).appendText("]");
                        }
                    }
                };
            }
        });
        bindRegex("\\[SKIN=([^]]+)\\]").to((placeholderManager, matcher) -> new SlotTemplate() {
            SlotTemplate args = BungeeTabListPlus.getInstance().getPlaceholderManager0().parseSlot(matcher.group(1));

            @Override
            public SlotBuilder buildSlot(SlotBuilder builder, TabListContext context) {
                return builder.setSkin(BungeeTabListPlus.getInstance().getSkinManager().getSkin(args.buildSlot(context).getText()));
            }
        });
        bind("name").alias("player").to(context -> {
            Optional<String> displayName = ((Player) context.getPlayer()).getOpt(MinecraftData.DisplayName);
            if (displayName.isPresent()) {
                return displayName.get();
            }
            if (context.getPlayer() instanceof ConnectedPlayer)
                return ((ConnectedPlayer) context.getPlayer()).getPlayer().getDisplayName();
            return context.getPlayer().getName();
        });
        bind("rawname").to(context -> context.getPlayer().getName());
        bind("server").to(context -> context.getServerGroup().map(ServerGroup::getName).orElse(""));
        bind("config_prefix").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getConfigPrefix(context, context.getPlayer()));
        bind("permprefix").alias("prefix").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getPrefix(context));
        bind("prefixColor").to(context -> {
            String prefix = BungeeTabListPlus.getInstance().getPermissionManager().getPrefix(context);
            if (prefix.length() < 2) {
                return prefix;
            }
            return prefix.substring(0, 2);
        });
        bind("prefixFormat").to(context -> {
            String prefix = BungeeTabListPlus.getInstance().getPermissionManager().getPrefix(context);
            if (prefix.length() < 4) {
                return prefix;
            }
            return prefix.substring(0, 4);
        });
        bind("suffixColor").to(context -> {
            String prefix = BungeeTabListPlus.getInstance().getPermissionManager().getSuffix(context.getPlayer());
            if (prefix.length() < 2) {
                return prefix;
            }
            return prefix.substring(0, 2);
        });
        bind("suffixFormat").to(context -> {
            String prefix = BungeeTabListPlus.getInstance().getPermissionManager().getSuffix(context.getPlayer());
            if (prefix.length() < 4) {
                return prefix;
            }
            return prefix.substring(0, 4);
        });
        bind("permsuffix").alias("suffix").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getSuffix(context.getPlayer()));
        bind("displayprefix").to(context -> ((Player) context.getPlayer()).getOpt(BungeeData.BungeePerms_DisplayPrefix).orElse(""));
        bind("bungeeperms_prefix").to(context -> ((Player) context.getPlayer()).getOpt(BungeeData.BungeePerms_Prefix).orElse(""));
        bind("bungeeperms_suffix").to(context -> ((Player) context.getPlayer()).getOpt(BungeeData.BungeePerms_Suffix).orElse(""));
        bind("bungeeperms_group").to(context -> ((Player) context.getPlayer()).getOpt(BungeeData.BungeePerms_PrimaryGroup).orElse(""));
        bind("bungeeperms_primary_group_prefix").to(context -> ((Player) context.getPlayer()).getOpt(BungeeData.BungeePerms_PrimaryGroupPrefix).orElse(""));
        bind("bungeeperms_player_prefix").to(context -> ((Player) context.getPlayer()).getOpt(BungeeData.BungeePerms_PlayerPrefix).orElse(""));
        bind("clientVersion").to(context -> ((Player) context.getPlayer()).getOpt(BungeeData.ClientVersion).orElse("unknown"));
        bind("ping").to(context -> String.format("%d", context.getPlayer().getPing()));
        bind("group").to(context -> BungeeTabListPlus.getInstance().getPermissionManager().getMainGroup(context.getPlayer()));
        bind("uuid").to(context -> context.getPlayer().getUniqueID().toString());
        bind("internalServerName").to(context -> context.getServer().map(ServerInfo::getName).orElse(""));
        bind("serverPrefix").withArgs().to((context, args) -> {
            if (args != null && !args.isEmpty()) {
                return BungeeTabListPlus.getInstance().getConfig().serverPrefixes.get(args);
            } else {
                return context.getServer().map(s -> BungeeTabListPlus.getInstance().getConfig().serverPrefixes.get(s.getName())).orElse("");
            }
        });
        bind("other_count").to(context -> {
            try {
                return String.format("%d", context.getOtherPlayerCount());
            } catch (IllegalStateException ignored) {
                // so someone is stupid enough to use {other_count} at some place
                // that is not in morePlayerLines
                return "\u2639"; // sad face
            }
        });
        bind("newline").to(context -> "\n");
    }
}
