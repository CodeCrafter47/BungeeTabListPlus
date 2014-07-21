/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.sorting;

import java.text.Collator;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class Alphabet implements ISortingRule {

    @Override
    public int compare(ProxiedPlayer player1, ProxiedPlayer player2) {
        String name1 = player1.getName();
        String name2 = player2.getName();
        return Collator.getInstance().compare(name1, name2);
    }

}
