/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
package codecrafter47.bungeetablistplus;

import java.util.LinkedList;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Florian Stober
 */
public class SendingQueue {

    private final List<ProxiedPlayer> liste = new LinkedList<>();

    public synchronized void addPlayer(ProxiedPlayer p) {
        if (!liste.contains(p)) {
            liste.add(p);
        }
    }

    public synchronized void addFrontPlayer(ProxiedPlayer p) {
        if (liste.contains(p)) {
            liste.remove(p);
        }
        liste.add(0, p);
    }

    public synchronized ProxiedPlayer getNext() {
        if (liste.isEmpty()) {
            return null;
        }
        ProxiedPlayer p = liste.remove(0);
        return p;
    }
}
