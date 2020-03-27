package codecrafter47.bungeetablistplus.command;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.command.util.CommandBase;
import codecrafter47.bungeetablistplus.command.util.CommandExecutor;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.util.chat.ChatUtil;
import com.google.common.base.Joiner;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import lombok.val;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CommandDebug extends CommandExecutor {

    public CommandDebug() {
        super("debug", "bungeetablistplus.admin");
        addSubCommand(new CommandBase("hidden", null, this::commandHidden));
    }

    private void commandHidden(CommandSender sender, String[] args) {

        ProxiedPlayer target = null;
        if (args.length == 0) {
            if (sender instanceof ProxiedPlayer) {
                target = (ProxiedPlayer) sender;
            } else {
                sender.sendMessage(ChatUtil.parseBBCode("&cUsage: [suggest=/btlp debug hidden ]/btlp debug hidden <name>[/suggest]"));
                return;
            }
        } else {
            target = ProxyServer.getInstance().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatUtil.parseBBCode("&cUnknown player: " + args[0]));
                return;
            }
        }

        val btlp = BungeeTabListPlus.getInstance();
        BungeePlayer player = btlp.getBungeePlayerProvider().getPlayerIfPresent(target);

        if (player == null) {
            sender.sendMessage(ChatUtil.parseBBCode("&cUnknown player: " + args[0]));
            return;
        }

        Runnable dummyListener = () -> {
        };
        CompletableFuture.runAsync(() -> {
            player.addDataChangeListener(MinecraftData.permission("bungeetablistplus.seevanished"), dummyListener);
            player.addDataChangeListener(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN, dummyListener);
        }, btlp.getMainThreadExecutor())
                .thenRun(() -> {
                    btlp.getMainThreadExecutor().schedule(() -> {
                        Boolean canSeeHiddenPlayers = player.get(MinecraftData.permission("bungeetablistplus.seevanished"));
                        Boolean isHidden = player.get(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN);
                        List<String> activeVanishProviders = btlp.getHiddenPlayersManager().getActiveVanishProviders(player);

                        player.removeDataChangeListener(MinecraftData.permission("bungeetablistplus.seevanished"), dummyListener);
                        player.removeDataChangeListener(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN, dummyListener);

                        sender.sendMessage(ChatUtil.parseBBCode("&bPlayer: &f" + player.getName() + "\n" +
                                "&bCan see hidden players: &f" + Boolean.TRUE.equals(canSeeHiddenPlayers) + "\n" +
                                "&bIs hidden: &f" + Boolean.TRUE.equals(isHidden) + ((!activeVanishProviders.isEmpty()) ? "\n" +
                                "&bHidden by: &f" + Joiner.on(", ").join(activeVanishProviders)
                                : "")));
                    }, 1, TimeUnit.SECONDS);
                });

    }
}
