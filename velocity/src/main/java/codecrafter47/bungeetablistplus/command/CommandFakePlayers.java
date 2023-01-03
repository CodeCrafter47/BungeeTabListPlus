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
import codecrafter47.bungeetablistplus.api.velocity.tablist.FakePlayer;
import codecrafter47.bungeetablistplus.player.FakePlayerManagerImpl;
import codecrafter47.bungeetablistplus.util.ProxyServer;
import codecrafter47.bungeetablistplus.util.ChatUtil;
import com.google.common.base.Joiner;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CommandFakePlayers {
    public static int commandAdd(CommandContext<CommandSource> context) {
        CommandSource sender = context.getSource();
        String name = context.getArgument("name", String.class);
        if (name == null) {
            sender.sendMessage(ChatUtil.parseBBCode("&cUsage: [suggest=/btlp fake add ]/btlp fake add <name>[/suggest]"));
        } else {
            FakePlayer fakePlayer = manager().createFakePlayer(name, randomServer(), false, true);
            fakePlayer.setRandomServerSwitchEnabled(true);
            sender.sendMessage(ChatUtil.parseBBCode("&aAdded fake player " + name + "."));
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int commandRemove(CommandContext<CommandSource> context) {
        CommandSource sender = context.getSource();
        String name = context.getArgument("name", String.class);
        if (name == null) {
            sender.sendMessage(ChatUtil.parseBBCode("&cUsage: [suggest=/btlp fake add ]/btlp fake remove <name>[/suggest]"));
        } else {
            List<FakePlayer> list = manager().getOnlineFakePlayers()
                    .stream()
                    .filter(player -> player.getName().equals(name))
                    .collect(Collectors.toList());
            if (list.isEmpty()) {
                sender.sendMessage(ChatUtil.parseBBCode("&cNo fake player with name " + name + " found."));
            } else {
                for (FakePlayer fakePlayer : list) {
                    manager().removeFakePlayer(fakePlayer);
                }
                if (list.size() == 1) {
                    sender.sendMessage(ChatUtil.parseBBCode("&aRemoved fake player " + name + "."));
                } else {
                    sender.sendMessage(ChatUtil.parseBBCode("&aRemoved " + list.size() + " fake players with name " + name + "."));
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int commandList(CommandContext<CommandSource> context) {
        CommandSource sender = context.getSource();
        Collection<FakePlayer> fakePlayers = manager().getOnlineFakePlayers();
        sender.sendMessage(ChatUtil.parseBBCode("&eThere are " + fakePlayers.size() + " fake players online: &f" + Joiner.on(", ").join(fakePlayers)));
        return Command.SINGLE_SUCCESS;
    }

    public static int commandRemoveAll(CommandContext<CommandSource> context) {
        CommandSource sender = context.getSource();
        Collection<FakePlayer> fakePlayers = manager().getOnlineFakePlayers();
        int count = 0;
        for (FakePlayer fakePlayer : fakePlayers) {
            manager().removeFakePlayer(fakePlayer);
            count++;
        }
        sender.sendMessage(ChatUtil.parseBBCode("&aRemoved " + count + " fake players."));
        return Command.SINGLE_SUCCESS;
    }

    public static int commandHelp(CommandContext<CommandSource> context) {
        CommandSource sender = context.getSource();
        sender.sendMessage(ChatUtil.parseBBCode("&e&lAvailable Commands:"));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest=/btlp fake add]/btlp fake add <name>[/suggest] &f&oAdd a fake player."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest=/btlp fake remove]/btlp fake remove <name>[/suggest] &f&oRemove a fake player."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake list[/suggest] &f&oShows a list of all fake players."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake removeall[/suggest] &f&oRemoves all fake players."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake help[/suggest] &f&oYou already found this one :P"));
        return Command.SINGLE_SUCCESS;
    }

    private static FakePlayerManagerImpl manager() {
        return BungeeTabListPlus.getInstance().getFakePlayerManagerImpl();
    }

    private static ServerInfo randomServer() {
        List<ServerInfo> servers = new ArrayList<>();
        for(RegisteredServer server : ProxyServer.getInstance().getAllServers()) servers.add(server.getServerInfo());
        return servers.get(ThreadLocalRandom.current().nextInt(servers.size()));
    }
}
