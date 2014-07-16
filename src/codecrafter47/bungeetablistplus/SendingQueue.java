/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus;

import java.util.LinkedList;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
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
