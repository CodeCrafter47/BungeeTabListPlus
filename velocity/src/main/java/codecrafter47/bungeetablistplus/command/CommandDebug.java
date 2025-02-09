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
import codecrafter47.bungeetablistplus.util.ProxyServer;
import codecrafter47.bungeetablistplus.util.chat.ChatUtil;
import com.google.common.base.Joiner;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.netty.channel.ChannelHandler;
import lombok.SneakyThrows;
import lombok.val;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommandDebug {
    public static int commandHidden(CommandContext<CommandSource> context) {
        CommandSource sender = context.getSource();
        Player target = null;
        String name = context.getArgument("name", String.class);


        if (name == null || name.trim().isEmpty()) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(ChatUtil.parseBBCode("&cUsage: [suggest=/btlp debug hidden ]/btlp debug hidden <name>[/suggest]"));
                return Command.SINGLE_SUCCESS;
            }
        } else {
            target = ProxyServer.getInstance().getPlayer(name).orElse(null);
            if (target == null) {
                sender.sendMessage(ChatUtil.parseBBCode("&cUnknown player: " + name));
                return Command.SINGLE_SUCCESS;
            }
        }

        val btlp = BungeeTabListPlus.getInstance();
        VelocityPlayer player = btlp.getVelocityPlayerProvider().getPlayerIfPresent(target);

        if (player == null) {
            sender.sendMessage(ChatUtil.parseBBCode("&cUnknown player: " + name));
            return Command.SINGLE_SUCCESS;
        }

        Runnable dummyListener = () -> {
        };
        CompletableFuture.runAsync(() -> {
            player.addDataChangeListener(BTLPVelocityDataKeys.permission("bungeetablistplus.seevanished"), dummyListener);
            player.addDataChangeListener(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN, dummyListener);
        }, btlp.getMainThreadExecutor())
                .thenRun(() -> {
                    btlp.getMainThreadExecutor().schedule(() -> {
                        Boolean canSeeHiddenPlayers = player.get(BTLPVelocityDataKeys.permission("bungeetablistplus.seevanished"));
                        Boolean isHidden = player.get(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN);
                        List<String> activeVanishProviders = btlp.getHiddenPlayersManager().getActiveVanishProviders(player);

                        player.removeDataChangeListener(BTLPVelocityDataKeys.permission("bungeetablistplus.seevanished"), dummyListener);
                        player.removeDataChangeListener(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN, dummyListener);

                        sender.sendMessage(ChatUtil.parseBBCode("&bPlayer: &f" + player.getName() + "\n" +
                                "&bCan see hidden players: &f" + Boolean.TRUE.equals(canSeeHiddenPlayers) + "\n" +
                                "&bIs hidden: &f" + Boolean.TRUE.equals(isHidden) + ((!activeVanishProviders.isEmpty()) ? "\n" +
                                "&bHidden by: &f" + Joiner.on(", ").join(activeVanishProviders)
                                : "")));
                    }, 1, TimeUnit.SECONDS);
                });
        return Command.SINGLE_SUCCESS;
    }

    @SneakyThrows
    public static int commandPipeline(CommandContext<CommandSource> context) {
        if(!(context.getSource() instanceof Player)) {
            context.getSource().sendMessage(ChatUtil.parseBBCode("&cThis command can only be run as a player!"));
            return Command.SINGLE_SUCCESS;
        }
        Player player = (Player) context.getSource();
        ConnectedPlayer userConnection = (ConnectedPlayer) player;
        List<String> userPipeline = new ArrayList<>();
        MinecraftConnection connection = ((ConnectedPlayer) player).getConnection();
        for (Map.Entry<String, ChannelHandler> entry : connection.getChannel().pipeline()) {
            userPipeline.add(entry.getKey());
        }

        VelocityServerConnection serverConnection = userConnection.getConnectedServer();
        List<String> serverPipeline = new ArrayList<>();
        for (Map.Entry<String, ChannelHandler> entry : serverConnection.getConnection().getChannel().pipeline()) {
            serverPipeline.add(entry.getKey());
        }

        player.sendMessage(ChatUtil.parseBBCode("&bUser: &f" + Joiner.on(", ").join(userPipeline) + "\n" +
                "&bServer: &f" + Joiner.on(", ").join(serverPipeline)));
        return Command.SINGLE_SUCCESS;
    }
}
