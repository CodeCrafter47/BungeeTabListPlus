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

package codecrafter47.bungeetablistplus.command;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import codecrafter47.bungeetablistplus.util.ChatUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class CommandHide {

    public static int commandToggle(CommandContext<CommandSource> context) {
        if(!(context.getSource() instanceof Player)) {
            context.getSource().sendMessage(ChatUtil.parseBBCode("&cThis command can only be run as a player!"));
            return Command.SINGLE_SUCCESS;
        }
        Player player = (Player) context.getSource();
        BungeeTabListPlus.getInstance().getMainThreadExecutor().execute(() -> {
            if (isHidden(player)) {
                unhidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYour name is no longer hidden from the tab list."));
            } else {
                hidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYou've been hidden from the tab list."));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int commandHide(CommandContext<CommandSource> context) {
        if(!(context.getSource() instanceof Player)) {
            context.getSource().sendMessage(ChatUtil.parseBBCode("&cThis command can only be run as a player!"));
            return Command.SINGLE_SUCCESS;
        }
        Player player = (Player) context.getSource();
        BungeeTabListPlus.getInstance().getMainThreadExecutor().execute(() -> {
            if (isHidden(player)) {
                player.sendMessage(ChatUtil.parseBBCode("&cYou're already hidden."));
            } else {
                hidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYou've been hidden from the tab list."));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    public static int commandUnhide(CommandContext<CommandSource> context) {
        if(!(context.getSource() instanceof Player)) {
            context.getSource().sendMessage(ChatUtil.parseBBCode("&cThis command can only be run as a player!"));
            return Command.SINGLE_SUCCESS;
        }
        Player player = (Player) context.getSource();
        BungeeTabListPlus.getInstance().getMainThreadExecutor().execute(() -> {
            if (isHidden(player)) {
                unhidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYour name is no longer hidden from the tab list."));
            } else {
                player.sendMessage(ChatUtil.parseBBCode("&cYou've not been hidden."));
            }
        });
        return Command.SINGLE_SUCCESS;
    }

    private static boolean isHidden(Player player) {
        VelocityPlayer velocityPlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayer(player);
        return Boolean.TRUE.equals(velocityPlayer.get(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_COMMAND));
    }

    private static void hidePlayer(Player player) {
        VelocityPlayer velocityPlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayer(player);
        velocityPlayer.getLocalDataCache().updateValue(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_COMMAND, true);
    }

    private static void unhidePlayer(Player player) {
        VelocityPlayer velocityPlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayer(player);
        velocityPlayer.getLocalDataCache().updateValue(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_COMMAND, false);
    }
}
