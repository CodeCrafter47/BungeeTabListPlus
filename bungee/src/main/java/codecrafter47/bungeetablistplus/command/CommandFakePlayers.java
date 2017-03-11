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
import codecrafter47.bungeetablistplus.api.bungee.tablist.FakePlayer;
import codecrafter47.bungeetablistplus.command.util.CommandBase;
import codecrafter47.bungeetablistplus.command.util.CommandExecutor;
import codecrafter47.bungeetablistplus.player.FakePlayerManagerImpl;
import codecrafter47.util.chat.ChatUtil;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CommandFakePlayers extends CommandExecutor {
    public CommandFakePlayers() {
        super("fakeplayers", "bungeetablistplus.admin", "fakeplayer", "fake");

        init();
    }

    private void init() {
        addSubCommand(new CommandBase("add", null, this::commandAdd));
        addSubCommand(new CommandBase("remove", null, this::commandRemove));
        addSubCommand(new CommandBase("list", null, this::commandList));
        addSubCommand(new CommandBase("removeall", null, this::commandRemoveAll));
        addSubCommand(new CommandBase("help", null, this::commandHelp));
        setDefaultAction(this::commandHelp);
    }

    private void commandAdd(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtil.parseBBCode("&cUsage: [suggest=/btlp fake add ]/btlp fake add <name>[/suggest]"));
        } else {
            for (String name : args) {
                FakePlayer fakePlayer = manager().createFakePlayer(name, randomServer(), false, true);
                fakePlayer.setRandomServerSwitchEnabled(true);
                sender.sendMessage(ChatUtil.parseBBCode("&aAdded fake player " + name + "."));
            }
        }
    }

    private void commandRemove(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatUtil.parseBBCode("&cUsage: [suggest=/btlp fake add ]/btlp fake remove <name>[/suggest]"));
        } else {
            for (String name : args) {
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
        }
    }

    private void commandList(CommandSender sender) {
        Collection<FakePlayer> fakePlayers = manager().getOnlineFakePlayers();
        sender.sendMessage(ChatUtil.parseBBCode("&eThere are " + fakePlayers.size() + " fake players online: &f" + Joiner.on(", ").join(fakePlayers)));
    }

    private void commandRemoveAll(CommandSender sender) {
        Collection<FakePlayer> fakePlayers = manager().getOnlineFakePlayers();
        int count = 0;
        for (FakePlayer fakePlayer : fakePlayers) {
            manager().removeFakePlayer(fakePlayer);
            count++;
        }
        sender.sendMessage(ChatUtil.parseBBCode("&aRemoved " + count + " fake players."));
    }

    private void commandHelp(CommandSender sender) {
        sender.sendMessage(ChatUtil.parseBBCode("&e&lAvailable Commands:"));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest=/btlp fake add]/btlp fake add <name>[/suggest] &f&oAdd a fake player."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest=/btlp fake remove]/btlp fake remove <name>[/suggest] &f&oRemove a fake player."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake list[/suggest] &f&oShows a list of all fake players."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake removeall[/suggest] &f&oRemoves all fake players."));
        sender.sendMessage(ChatUtil.parseBBCode("&e[suggest]/btlp fake help[/suggest] &f&oYou already found this one :P"));
    }

    private static FakePlayerManagerImpl manager() {
        return BungeeTabListPlus.getInstance().getFakePlayerManagerImpl();
    }

    private static ServerInfo randomServer() {
        ArrayList<ServerInfo> servers = new ArrayList<>(ProxyServer.getInstance().getServers().values());
        return servers.get(ThreadLocalRandom.current().nextInt(servers.size()));
    }
}
