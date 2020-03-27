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

package codecrafter47.bungeetablistplus.command;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.command.util.CommandBase;
import codecrafter47.bungeetablistplus.command.util.CommandExecutor;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.util.chat.ChatUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import static codecrafter47.bungeetablistplus.command.util.CommandBase.playerCommand;

public class CommandHide extends CommandExecutor {
    public CommandHide() {
        super("hide", "bungeetablistplus.hide", "vanish", "v");

        init();
    }

    private void init() {
        addSubCommand(new CommandBase("on", null, playerCommand(this::commandHide), "true", "enable"));
        addSubCommand(new CommandBase("off", null, playerCommand(this::commandUnhide), "false", "disable"));
        addSubCommand(new CommandBase("toggle", null, playerCommand(this::commandToggle)));
        setDefaultAction(playerCommand(this::commandToggle));
    }

    private void commandToggle(ProxiedPlayer player) {
        BungeeTabListPlus.getInstance().getMainThreadExecutor().execute(() -> {
            if (isHidden(player)) {
                unhidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYour name is no longer hidden from the tab list."));
            } else {
                hidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYou've been hidden from the tab list."));
            }
        });
    }

    private void commandHide(ProxiedPlayer player) {
        BungeeTabListPlus.getInstance().getMainThreadExecutor().execute(() -> {
            if (isHidden(player)) {
                player.sendMessage(ChatUtil.parseBBCode("&cYou're already hidden."));
            } else {
                hidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYou've been hidden from the tab list."));
            }
        });
    }

    private void commandUnhide(ProxiedPlayer player) {
        BungeeTabListPlus.getInstance().getMainThreadExecutor().execute(() -> {
            if (isHidden(player)) {
                unhidePlayer(player);
                player.sendMessage(ChatUtil.parseBBCode("&aYour name is no longer hidden from the tab list."));
            } else {
                player.sendMessage(ChatUtil.parseBBCode("&cYou've not been hidden."));
            }
        });
    }

    private boolean isHidden(ProxiedPlayer player) {
        BungeePlayer bungeePlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayer(player);
        return Boolean.TRUE.equals(bungeePlayer.get(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_COMMAND));
    }

    private void hidePlayer(ProxiedPlayer player) {
        BungeePlayer bungeePlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayer(player);
        bungeePlayer.getLocalDataCache().updateValue(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_COMMAND, true);
    }

    private void unhidePlayer(ProxiedPlayer player) {
        BungeePlayer bungeePlayer = BungeeTabListPlus.getInstance().getBungeePlayerProvider().getPlayer(player);
        bungeePlayer.getLocalDataCache().updateValue(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_COMMAND, false);
    }
}
