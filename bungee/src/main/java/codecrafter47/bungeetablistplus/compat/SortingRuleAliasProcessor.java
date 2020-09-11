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

package codecrafter47.bungeetablistplus.compat;

import com.google.common.collect.ImmutableMap;
import de.codecrafter47.taboverlay.config.ErrorHandler;
import de.codecrafter47.taboverlay.config.SortingRulePreprocessor;
import lombok.Value;
import org.yaml.snakeyaml.error.Mark;

public class SortingRuleAliasProcessor implements SortingRulePreprocessor {

    private static final ImmutableMap<String, RewriteData> map = ImmutableMap.<String, RewriteData>builder()
            .put("you", new RewriteData("name viewer-first", true))
            .put("youfirst", new RewriteData("name viewer-first", false))
            .put("alpha", new RewriteData("name", true))
            .put("alphabet", new RewriteData("name", true))
            .put("alphabetic", new RewriteData("name", true))
            .put("alphabetical", new RewriteData("name", true))
            .put("alphabetically", new RewriteData("name", false))
            .put("teamfirst", new RewriteData("team viewer-first", false))
            .put("teams", new RewriteData("team", false))
            .put("factionfirst", new RewriteData("faction_name viewer-first", false))
            .put("factions", new RewriteData("faction_name", false))
            .put("worldname", new RewriteData("world", true))
            .put("playerworld", new RewriteData("world viewer-first", true))
            .put("playerworldfirst", new RewriteData("world viewer-first", true))
            .put("serveralphabetically", new RewriteData("server", true))
            .put("playerserverfirst", new RewriteData("server viewer-first", true))
            .put("afklast", new RewriteData("essentials_afk as number asc", true))
            .put("vaultgroupinfo", new RewriteData("vault_primary_group_weight asc", true))
            .put("vaultgroupinforeversed", new RewriteData("vault_primary_group_weight desc", true))
            .put("bungeepermsgroupinfo", new RewriteData("bungeeperms_primary_group_weight asc", true))
            .put("bungeepermsgroupinforeversed", new RewriteData("bungeeperms_primary_group_weight desc", true))
            .put("luckpermsgroupinfo", new RewriteData("luckpermsbungee_primary_group_weight asc", true))
            .put("luckpermsgroupinforeversed", new RewriteData("luckpermsbungee_primary_group_weight desc", true))
            .put("vaultprefix", new RewriteData("vault_prefix asc", true))
            .put("connectedfirst", new RewriteData("session_duration_total_seconds desc", false))
            .put("connectedlast", new RewriteData("session_duration_total_seconds asc", false))
            .build();

    @Override
    public String process(String sortingRule, ErrorHandler errorHandler, Mark mark) {
        RewriteData rewriteData = map.get(sortingRule.toLowerCase());
        if (rewriteData != null) {
            if (rewriteData.deprecated) {
                errorHandler.addWarning("Sorting rule '" + sortingRule + "' has been deprecated. Use '" + rewriteData.rewrite + "' instead.", mark);
            }
            return rewriteData.rewrite;
        }
        return sortingRule;
    }

    @Value
    private static class RewriteData {
        String rewrite;
        boolean deprecated;
    }
}
