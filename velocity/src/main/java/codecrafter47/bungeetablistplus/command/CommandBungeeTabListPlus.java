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
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.updater.UpdateChecker;
import codecrafter47.bungeetablistplus.util.VelocityPlugin;
import codecrafter47.bungeetablistplus.util.chat.ChatUtil;
import com.google.common.base.Joiner;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.codecrafter47.data.api.DataHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandBungeeTabListPlus {

    private final VelocityPlugin plugin;
    public CommandBungeeTabListPlus(VelocityPlugin plugin) {
        this.plugin = plugin;
    }

    public BrigadierCommand register(String base) {
        LiteralCommandNode<CommandSource> btlpCommand = LiteralArgumentBuilder.<CommandSource>literal(base)
                .requires(source -> source.hasPermission("bungeetablistplus.command"))
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload").requires(source -> source.hasPermission("bungeetablistplus.admin")).executes(this::commandReload))
                .then(LiteralArgumentBuilder.<CommandSource>literal("status").executes(this::commandStatus))
                .then(LiteralArgumentBuilder.<CommandSource>literal("help").executes(this::commandHelp))
                .then(LiteralArgumentBuilder.<CommandSource>literal("debug").requires(source -> source.hasPermission("bungeetablistplus.admin"))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("hidden").executes(CommandDebug::commandHidden))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("pipeline").executes(CommandDebug::commandPipeline))
                ).then(LiteralArgumentBuilder.<CommandSource>literal("hide").requires(source -> source.hasPermission("bungeetablistplus.hide"))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("on").executes(CommandHide::commandHide))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("true").executes(CommandHide::commandHide))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("enable").executes(CommandHide::commandHide))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("off").executes(CommandHide::commandUnhide))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("false").executes(CommandHide::commandUnhide))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("disable").executes(CommandHide::commandUnhide))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("toggle").executes(CommandHide::commandToggle))
                        .executes(CommandHide::commandToggle)
                ).then(LiteralArgumentBuilder.<CommandSource>literal("fakeplayers").requires(source -> source.hasPermission("bungeetablistplus.admin"))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word()).executes(CommandFakePlayers::commandAdd)))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("remove")
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("name", StringArgumentType.word()).executes(CommandFakePlayers::commandRemove)))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("list").executes(CommandFakePlayers::commandList))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("removeall").executes(CommandFakePlayers::commandRemoveAll))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("help").executes(CommandFakePlayers::commandHelp))
                        .executes(CommandFakePlayers::commandRemove)
                )
                .executes(this::commandHelp)
                .build();
        return new BrigadierCommand(btlpCommand);
    }

    private int commandReload(CommandContext<CommandSource> ctx) {
        CommandSource sender = ctx.getSource();
        boolean success = BungeeTabListPlus.getInstance().reload();
        if (success) {
            sender.sendMessage(Component.text("Successfully reloaded BungeeTabListPlus.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("An error occurred while reloaded BungeeTabListPlus.", NamedTextColor.RED));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int commandHelp(CommandContext<CommandSource> ctx) {
        CommandSource sender = ctx.getSource();
        sender.sendMessage(ChatUtil.parseBBCode("&e&lAvailable Commands:"));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp reload[/suggest] &f&oReload the configuration"));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest=/btlp hide]/btlp hide [on|off|toggle][/suggest] &f&oHide yourself from the tab list."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake[/suggest] &f&oManage fake players."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp status[/suggest] &f&oDisplays info about plugin version, updates and the bridge plugin."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp help[/suggest] &f&oYou already found this one :P"));
        return Command.SINGLE_SUCCESS;
    }

    private int commandStatus(CommandContext<CommandSource> ctx) {
        CommandSource sender = ctx.getSource();
        // Version
        String version = plugin.getVersion();
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
        List<RegisteredServer> servers = new ArrayList<>(plugin.getProxy().getAllServers());
        List<String> withBridge = new ArrayList<>();
        List<RegisteredServer> withoutBridge = new ArrayList<>();
        List<String> maybeBridge = new ArrayList<>();
        for (RegisteredServer server : servers) {
            DataHolder dataHolder = bridge.getServerDataHolder(server.getServerInfo().getName());
            if (dataHolder != null && dataHolder.get(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES) != null) {
                withBridge.add(server.getServerInfo().getName());
            } else {
                if (server.getPlayersConnected().isEmpty()) {
                    maybeBridge.add(server.getServerInfo().getName());
                } else {
                    withoutBridge.add(server);
                }
            }
        }
        List<String> withPAPI = servers.stream()
                .filter(server -> {
                    DataHolder dataHolder = bridge.getServerDataHolder(server.getServerInfo().getName());
                    Boolean b;
                    return dataHolder != null && (b = dataHolder.get(BTLPDataKeys.PLACEHOLDERAPI_PRESENT)) != null && b;
                })
                .map((server) -> server.getServerInfo().getName())
                .collect(Collectors.toList());

        sender.sendMessage(ChatUtil.parseBBCode("&eBridge plugin status:"));
        if (!withBridge.isEmpty()) {
            sender.sendMessage(ChatUtil.parseBBCode("&fInstalled on: &a" + Joiner.on("&f,&a ").join(withBridge)));
        }
        if (!withPAPI.isEmpty()) {
            sender.sendMessage(ChatUtil.parseBBCode("&fServers with PlaceholderAPI: &a" + Joiner.on("&f, &a").join(withPAPI)));
        }
        for (RegisteredServer server : withoutBridge) {
            sender.sendMessage(ChatUtil.parseBBCode("&c" + server.getServerInfo().getName() + "&f: " + bridge.getStatus(server)));
        }
        if (!maybeBridge.isEmpty()) {
            sender.sendMessage(ChatUtil.parseBBCode("&eBridge status is not available for servers without players."));
        }

        // That's it
        sender.sendMessage(ChatUtil.parseBBCode("&aThank you for using BungeeTabListPlus."));
        return Command.SINGLE_SUCCESS;
    }
}
