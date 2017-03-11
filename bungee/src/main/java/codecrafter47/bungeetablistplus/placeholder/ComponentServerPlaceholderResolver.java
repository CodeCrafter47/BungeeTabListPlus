package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.BTLPContextKeys;
import codecrafter47.bungeetablistplus.managers.DataManager;
import de.codecrafter47.data.api.TypeToken;
import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.placeholder.*;
import de.codecrafter47.taboverlay.config.player.Player;
import de.codecrafter47.taboverlay.config.player.PlayerSet;
import de.codecrafter47.taboverlay.config.template.TemplateCreationContext;
import de.codecrafter47.taboverlay.config.view.AbstractActiveElement;

import javax.annotation.Nonnull;
import java.util.List;

public class ComponentServerPlaceholderResolver implements PlaceholderResolver<Context> {
    private final ServerPlaceholderResolver serverPlaceholderResolver;
    private final DataManager dataManager;

    public ComponentServerPlaceholderResolver(ServerPlaceholderResolver serverPlaceholderResolver, DataManager dataManager) {
        this.serverPlaceholderResolver = serverPlaceholderResolver;
        this.dataManager = dataManager;
    }

    @Nonnull
    @Override
    public PlaceholderBuilder<?, ?> resolve(PlaceholderBuilder<Context, ?> builder, List<PlaceholderArg> args, TemplateCreationContext tcc) throws UnknownPlaceholderException, PlaceholderException {
        if (args.size() >= 1 && "server".equalsIgnoreCase(args.get(0).getText())) {
            args.remove(0);
            try {
                return serverPlaceholderResolver.resolve(builder.transformContext(context -> dataManager.getServerDataHolder(context.getCustomObject(BTLPContextKeys.SERVER_ID))), args, tcc);
            } catch (UnknownPlaceholderException e) {
                throw new PlaceholderException("Unknown Placeholder");
            }
        }
        if (args.size() >= 1 && "server_player_count".equalsIgnoreCase(args.get(0).getText())) {
            args.remove(0);

            return builder.acquireData(ServerPlayerCountPlaceholder::new, TypeToken.INTEGER);
        }
        throw new UnknownPlaceholderException();
    }

    private static class ServerPlayerCountPlaceholder extends AbstractActiveElement<Runnable> implements PlaceholderDataProvider<Context, Integer>, PlayerSet.Listener {

        private PlayerSet playerSet;

        @Override
        protected void onActivation() {
            playerSet = getContext().getCustomObject(BTLPContextKeys.SERVER_PLAYER_SET);
            playerSet.addListener(this);
        }

        @Override
        public Integer getData() {
            return playerSet.getCount();
        }

        @Override
        protected void onDeactivation() {
            playerSet.removeListener(this);
        }

        @Override
        public void onPlayerAdded(Player player) {
            if (hasListener()) {
                getListener().run();
            }
        }

        @Override
        public void onPlayerRemoved(Player player) {
            if (hasListener()) {
                getListener().run();
            }
        }
    }
}
