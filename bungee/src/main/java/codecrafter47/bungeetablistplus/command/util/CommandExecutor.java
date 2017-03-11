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

package codecrafter47.bungeetablistplus.command.util;

import codecrafter47.util.chat.ChatUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.util.CaseInsensitiveMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CommandExecutor extends Command implements TabExecutor {
    private final Map<String, Command> subCommands = new CaseInsensitiveMap<>();
    private Consumer<CommandSender> defaultAction = null;

    public CommandExecutor(String name) {
        super(name);
    }

    public CommandExecutor(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    public void addSubCommand(Command command) {
        subCommands.put(command.getName(), command);
        for (String alias : command.getAliases()) {
            subCommands.put(alias, command);
        }
    }

    public void setDefaultAction(Consumer<CommandSender> defaultAction) {
        this.defaultAction = defaultAction;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && subCommands.containsKey(args[0])) {
            Command command = subCommands.get(args[0]);
            if (command.getPermission() == null || checkPermission(sender, command)) {
                command.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            } else {
                sender.sendMessage(TextComponent.fromLegacyText(ProxyServer.getInstance().getTranslation("no_permission")));
            }
        } else if (defaultAction != null) {
            defaultAction.accept(sender);
        } else {
            sender.sendMessage(ChatUtil.parseBBCode("&cWrong usage!"));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(cmd -> cmd.startsWith(args[0]))
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            Command command = subCommands.get(args[0]);
            if (command instanceof TabExecutor) {
                if (command.getPermission() == null || checkPermission(sender, command)) {
                    ((TabExecutor) command).onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
                }
            }
        }
        return Collections.emptyList();
    }

    private static boolean checkPermission(CommandSender sender, Command command) {
        return sender.hasPermission(command.getPermission());
    }
}
