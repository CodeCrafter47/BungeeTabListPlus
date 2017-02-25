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

package codecrafter47.bungeetablistplus.playersorting;

import codecrafter47.bungeetablistplus.playersorting.rules.*;
import com.google.common.collect.ImmutableMap;

import java.util.Optional;

public class SortingRuleRegistry {
    private static final ImmutableMap<String, SortingRule> map = ImmutableMap.<String, SortingRule>builder()
            .put("you", new YouFirst())
            .put("youfirst", new YouFirst())
            .put("alpha", new Alphabet())
            .put("alphabet", new Alphabet())
            .put("alphabetic", new Alphabet())
            .put("alphabetical", new Alphabet())
            .put("alphabetically", new Alphabet())
            .put("teamfirst", new TeamFirst())
            .put("teams", new TeamsAlphabetically())
            .put("factionfirst", new FactionFirst())
            .put("factions", new FactionsAlphabetically())
            .put("worldname", new WorldByName())
            .put("playerworld", new PlayerWorld())
            .put("playerworldfirst", new PlayerWorld())
            .put("serveralphabetically", new ServerAlphabetically())
            .put("playerserverfirst", new PlayerServerFirst())
            .put("afklast", new AFKLast())
            .put("vaultgroupinfo", new VaultGroupInfo())
            .put("vaultgroupinforeversed", new ReverseOrder(new VaultGroupInfo()))
            .put("bungeepermsgroupinfo", new BungeePermsGroupInfo())
            .put("bungeepermsgroupinforeversed", new ReverseOrder(new BungeePermsGroupInfo()))
            .put("luckpermsgroupinfo", new LuckPermsGroupInfo())
            .put("luckpermsgroupinforeversed", new ReverseOrder(new LuckPermsGroupInfo()))
            .put("bungeecordgroups", new BungeeCordGroups())
            .put("bungeecordgroupsreversed", new ReverseOrder(new BungeeCordGroups()))
            .put("vaultprefix", new VaultPrefixAlphabetically())
            .put("connectedfirst", new ReverseOrder(new ConnectedLast()))
            .put("connectedlast", new ConnectedLast())
            .build();

    public static Optional<SortingRule> getRule(String name) {
        return Optional.ofNullable(map.get(name.toLowerCase()));
    }
}
