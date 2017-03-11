package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.managers.DataManager;
import de.codecrafter47.data.api.TypeToken;
import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.placeholder.*;
import de.codecrafter47.taboverlay.config.template.TemplateCreationContext;

import javax.annotation.Nonnull;
import java.util.List;

public class ServerCountPlaceholderResolver implements PlaceholderResolver<Context> {

    private final DataManager dataManager;

    public ServerCountPlaceholderResolver(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Nonnull
    @Override
    public PlaceholderBuilder<?, ?> resolve(PlaceholderBuilder<Context, ?> builder, List<PlaceholderArg> args, TemplateCreationContext tcc) throws UnknownPlaceholderException, PlaceholderException {
        if (args.size() >= 2 && "server_count".equalsIgnoreCase(args.get(0).getText()) && "total".equalsIgnoreCase(args.get(1).getText())) {
            args.remove(0);
            args.remove(0);
            return builder.transformContext(context -> dataManager.getProxyData())
                    .acquireData(new DataHolderPlaceholderDataProviderSupplier<>(TypeToken.INTEGER, BTLPBungeeDataKeys.DATA_KEY_Server_Count, (server, replacement) -> replacement), TypeToken.INTEGER);
        }
        if (args.size() >= 2 && "server_count".equalsIgnoreCase(args.get(0).getText()) && "online".equalsIgnoreCase(args.get(1).getText())) {
            args.remove(0);
            args.remove(0);
            return builder.transformContext(context -> dataManager.getProxyData())
                    .acquireData(new DataHolderPlaceholderDataProviderSupplier<>(TypeToken.INTEGER, BTLPBungeeDataKeys.DATA_KEY_Server_Count_Online, (server, replacement) -> replacement), TypeToken.INTEGER);
        }
        throw new UnknownPlaceholderException();
    }

}
