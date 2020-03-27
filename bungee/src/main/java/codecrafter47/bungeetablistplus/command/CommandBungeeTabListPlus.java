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
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.command.util.CommandBase;
import codecrafter47.bungeetablistplus.command.util.CommandExecutor;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.updater.UpdateChecker;
import codecrafter47.util.chat.ChatUtil;
import com.google.common.base.Joiner;
import de.codecrafter47.data.api.DataHolder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandBungeeTabListPlus extends CommandExecutor {
    public CommandBungeeTabListPlus() {
        super("BungeeTabListPlus", "bungeetablistplus.command", "btlp");

        init();
    }

    private void init() {
        addSubCommand(new CommandBase("reload", "bungeetablistplus.admin", this::commandReload));
        addSubCommand(new CommandHide());
        addSubCommand(new CommandFakePlayers());
        addSubCommand(new CommandBase("help", null, this::commandHelp, "?"));
        addSubCommand(new CommandBase("status", null, this::commandStatus));
        addSubCommand(new CommandDebug());
        setDefaultAction(this::commandHelp);
    }

    private void commandReload(CommandSender sender) {
        boolean success = BungeeTabListPlus.getInstance().reload();
        if (success) {
            sender.sendMessage(ChatUtil.parseBBCode("&aSuccessfully reloaded BungeeTabListPlus."));
        } else {
            sender.sendMessage(ChatUtil.parseBBCode("&cAn error occurred while reloaded BungeeTabListPlus."));
        }
    }

    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatUtil.parseBBCode("&e&lAvailable Commands:"));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp reload[/suggest] &f&oReload the configuration"));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest=/btlp hide]/btlp hide [on|off|toggle][/suggest] &f&oHide yourself from the tab list."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake[/suggest] &f&oManage fake players."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp status[/suggest] &f&oDisplays info about plugin version, updates and the bridge plugin."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp help[/suggest] &f&oYou already found this one :P"));
    }

    private void commandStatus(CommandSender sender) {
        // Version
        String version = BungeeTabListPlus.getInstance().getPlugin().getDescription().getVersion();
        sender.sendMessage(ChatUtil.parseBBCode("&eYou are running BungeeTabListPlus version " + version));

        // Update?
        sender.sendMessage(ChatUtil.parseBBCode("&eLooking for an update..."));
        UpdateChecker updateChecker = new UpdateChecker(BungeeTabListPlus.getInstance().getPlugin());
        updateChecker.run();
        if (updateChecker.isUpdateAvailable()) {
            sender.sendMessage(ChatUtil.parseBBCode("&aAn update is available at [url]http://www.spigotmc.org/resources/bungeetablistplus.313/[/url]"));
        } else if (updateChecker.isNewDevBuildAvailable()) {
            sender.sendMessage(ChatUtil.parseBBCode("&aA new dev-build is available at [url]https://ci.codecrafter47.de/job/BungeeTabListPlus/[/url]"));
        } else {
            sender.sendMessage(ChatUtil.parseBBCode("&aYou are already running the latest version."));
        }

        // Bridge plugin status
        BukkitBridge bridge = BungeeTabListPlus.getInstance().getBridge();
        List<ServerInfo> servers = new ArrayList<>(ProxyServer.getInstance().getServers().values());
        List<String> withBridge = new ArrayList<>();
        List<ServerInfo> withoutBridge = new ArrayList<>();
        List<String> maybeBridge = new ArrayList<>();
        for (ServerInfo server : servers) {
            DataHolder dataHolder = bridge.getServerDataHolder(server.getName());
            if (dataHolder != null && dataHolder.get(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES) != null) {
                withBridge.add(server.getName());
            } else {
                if (server.getPlayers().isEmpty()) {
                    maybeBridge.add(server.getName());
                } else {
                    withoutBridge.add(server);
                }
            }
        }
        List<String> withPAPI = servers.stream()
                .filter(server -> {
                    DataHolder dataHolder = bridge.getServerDataHolder(server.getName());
                    Boolean b;
                    return dataHolder != null && (b = dataHolder.get(BTLPDataKeys.PLACEHOLDERAPI_PRESENT)) != null && b;
                })
                .map(ServerInfo::getName)
                .collect(Collectors.toList());

        sender.sendMessage(ChatUtil.parseBBCode("&eBridge plugin status:"));
        if (!withBridge.isEmpty()) {
            sender.sendMessage(ChatUtil.parseBBCode("&fInstalled on: &a" + Joiner.on("&f,&a ").join(withBridge)));
        }
        if (!withPAPI.isEmpty()) {
            sender.sendMessage(ChatUtil.parseBBCode("&fServers with PlaceholderAPI: &a" + Joiner.on("&f, &a").join(withPAPI)));
        }
        for (ServerInfo server : withoutBridge) {
            sender.sendMessage(ChatUtil.parseBBCode("&c" + server.getName() + "&f: " + bridge.getStatus(server)));
        }
        if (!maybeBridge.isEmpty()) {
            sender.sendMessage(ChatUtil.parseBBCode("&eBridge status is not available for servers without players."));
        }

        // That's it
        sender.sendMessage(ChatUtil.parseBBCode("&aThank you for using BungeeTabListPlus."));
    }
}
