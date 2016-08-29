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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CommandBase extends Command {
    private final BiConsumer<CommandSender, String[]> action;

    public CommandBase(String name, String permission, Consumer<CommandSender> action, String... aliases) {
        this(name, permission, (a, b) -> action.accept(a), aliases);
    }

    public CommandBase(String name, String permission, BiConsumer<CommandSender, String[]> action, String... aliases) {
        super(name, permission, aliases);
        this.action = action;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        action.accept(sender, args);
    }

    public static Consumer<CommandSender> playerCommand(Consumer<ProxiedPlayer> cmd) {
        return sender -> {
            if (sender instanceof ProxiedPlayer) {
                cmd.accept(((ProxiedPlayer) sender));
            } else {
                sender.sendMessage(ChatUtil.parseBBCode("&cThis command can only be used ingame."));
            }
        };
    }
}
