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

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.managers.DataManager;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.placeholder.*;
import de.codecrafter47.taboverlay.config.template.TemplateCreationContext;

import javax.annotation.Nonnull;
import java.util.List;

public class GlobalServerPlaceholderResolver implements PlaceholderResolver<Context> {

    private final DataManager dataManager;
    private final ServerPlaceholderResolver serverPlaceholderResolver;

    public GlobalServerPlaceholderResolver(DataManager dataManager, ServerPlaceholderResolver serverPlaceholderResolver) {
        this.dataManager = dataManager;
        this.serverPlaceholderResolver = serverPlaceholderResolver;
    }

    @Nonnull
    @Override
    public PlaceholderBuilder<?, ?> resolve(PlaceholderBuilder<Context, ?> builder, List<PlaceholderArg> args, TemplateCreationContext tcc) throws UnknownPlaceholderException, PlaceholderException {
        if (args.size() >= 1 && args.get(0) instanceof PlaceholderArg.Text && args.get(0).getText().startsWith("server:")) {
            String serverName = args.remove(0).getText().substring(7);
            DataHolder serverDataHolder = dataManager.getServerDataHolder(serverName);
            return serverPlaceholderResolver.resolve(builder.transformContext(context -> serverDataHolder), args, tcc);
        }
        throw new UnknownPlaceholderException();
    }

}
