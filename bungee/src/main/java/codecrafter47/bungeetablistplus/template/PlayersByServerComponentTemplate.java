package codecrafter47.bungeetablistplus.template;

import codecrafter47.bungeetablistplus.BTLPContextKeys;
import codecrafter47.bungeetablistplus.config.PlayersByServerComponentConfiguration;
import codecrafter47.bungeetablistplus.util.ContextAwareOrdering;
import codecrafter47.bungeetablistplus.view.PlayersByServerComponentView;
import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.expression.template.ExpressionTemplate;
import de.codecrafter47.taboverlay.config.player.PlayerSetFactory;
import de.codecrafter47.taboverlay.config.player.PlayerSetPartition;
import de.codecrafter47.taboverlay.config.template.PlayerOrderTemplate;
import de.codecrafter47.taboverlay.config.template.PlayerSetTemplate;
import de.codecrafter47.taboverlay.config.template.component.ComponentTemplate;
import de.codecrafter47.taboverlay.config.template.icon.IconTemplate;
import de.codecrafter47.taboverlay.config.template.ping.PingTemplate;
import de.codecrafter47.taboverlay.config.template.text.TextTemplate;
import de.codecrafter47.taboverlay.config.view.components.ComponentView;
import de.codecrafter47.taboverlay.config.view.components.ContainerComponentView;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Function;

@Value
@Builder
public class PlayersByServerComponentTemplate implements ComponentTemplate {
    PlayerSetTemplate playerSet;

    PlayerSetFactory playerSetFactory;

    PlayerOrderTemplate playerOrder;

    ComponentTemplate playerComponent;

    @NonNull
    @Nonnull
    ComponentTemplate morePlayersComponent;

    @Nullable
    ComponentTemplate serverHeader;

    @Nullable
    ComponentTemplate serverFooter;

    @Nullable
    ComponentTemplate serverSeparator;

    boolean fillSlotsVertical;
    int minSize;
    /* A value of -1 indicates no limit. */
    int maxSize;
    int minSizePerServer;
    /* A value of -1 indicates no limit. */
    int maxSizePerServer;
    int columns;

    TextTemplate defaultText;
    PingTemplate defaultPing;
    IconTemplate defaultIcon;

    ExpressionTemplate partitionFunction;
    Function<String, String> mergeSections;

    Set<String> hiddenServers;
    PlayersByServerComponentConfiguration.ServerOptions showServers;
    @Nullable
    ContextAwareOrdering<Context, PlayerSetPartition, String> serverComparator;

    boolean prioritizeViewerServer;

    @Override
    public LayoutInfo getLayoutInfo() {
        return LayoutInfo.builder()
                .constantSize(false)
                .minSize(0)
                .blockAligned(true)
                .build();
    }

    @Override
    public ComponentView instantiate() {
        return new ContainerComponentView(new PlayersByServerComponentView(fillSlotsVertical ? 1 : columns, playerSet, playerComponent, playerComponent.getLayoutInfo().getMinSize(), morePlayersComponent, morePlayersComponent.getLayoutInfo().getMinSize(), playerOrder, defaultText, defaultPing, defaultIcon, partitionFunction, mergeSections, serverHeader, serverFooter, serverSeparator, minSizePerServer, maxSizePerServer, (parent, sectionId, playerSet1) -> {
            Context child = parent.clone();
            child.setCustomObject(BTLPContextKeys.SERVER_ID, sectionId);
            child.setCustomObject(BTLPContextKeys.SERVER_PLAYER_SET, playerSet1);
            return child;
        }, hiddenServers, showServers, serverComparator, prioritizeViewerServer),
                fillSlotsVertical, minSize, maxSize, columns, false);
    }
}
