package codecrafter47.bungeetablistplus.config;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.placeholder.ComponentServerPlaceholderResolver;
import codecrafter47.bungeetablistplus.template.PlayersByServerComponentTemplate;
import codecrafter47.bungeetablistplus.util.ContextAwareOrdering;
import codecrafter47.bungeetablistplus.util.MapFunction;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.dsl.ComponentConfiguration;
import de.codecrafter47.taboverlay.config.dsl.PlayerOrderConfiguration;
import de.codecrafter47.taboverlay.config.dsl.components.BasicComponentConfiguration;
import de.codecrafter47.taboverlay.config.dsl.util.ConfigValidationUtil;
import de.codecrafter47.taboverlay.config.dsl.yaml.*;
import de.codecrafter47.taboverlay.config.placeholder.OtherCountPlaceholderResolver;
import de.codecrafter47.taboverlay.config.placeholder.PlayerPlaceholderResolver;
import de.codecrafter47.taboverlay.config.player.PlayerSet;
import de.codecrafter47.taboverlay.config.player.PlayerSetPartition;
import de.codecrafter47.taboverlay.config.template.PlayerOrderTemplate;
import de.codecrafter47.taboverlay.config.template.TemplateCreationContext;
import de.codecrafter47.taboverlay.config.template.component.ComponentTemplate;
import de.codecrafter47.taboverlay.config.template.icon.PlayerIconTemplate;
import de.codecrafter47.taboverlay.config.template.ping.PlayerPingTemplate;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.yaml.snakeyaml.error.Mark;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
public class PlayersByServerComponentConfiguration extends MarkedPropertyBase implements ComponentConfiguration {

    private PlayerOrderConfiguration playerOrder = PlayerOrderConfiguration.DEFAULT;
    private MarkedStringProperty playerSet;
    private ComponentConfiguration playerComponent = new BasicComponentConfiguration("${player name}");
    @Nullable
    private ComponentConfiguration morePlayersComponent;
    private boolean fillSlotsVertical = false;
    private MarkedIntegerProperty minSize = new MarkedIntegerProperty(0);
    private MarkedIntegerProperty maxSize = new MarkedIntegerProperty(-1);
    private MarkedIntegerProperty minSizePerServer = new MarkedIntegerProperty(0);
    private MarkedIntegerProperty maxSizePerServer = new MarkedIntegerProperty(-1);
    @Nullable
    private ComponentConfiguration serverHeader;
    @Nullable
    private ComponentConfiguration serverFooter;
    @Nullable
    private ComponentConfiguration serverSeparator;

    private MarkedListProperty<String> hiddenServers = new MarkedListProperty<>();

    private ServerOptions showServers = null;
    private MarkedBooleanProperty includeEmptyServers = null;

    private MarkedStringProperty serverOrder = null;
    private Map<String, Integer> customServerOrder = null;
    private boolean prioritizeViewerServer = true;

    private Map<String, List<String>> mergeServers = null;

    public List<String> getCustomServerOrder() {
        // dummy method for snakeyaml to detect property type
        return null;
    }

    public void setCustomServerOrder(List<String> customServerOrder) {
        if (customServerOrder == null) {
            this.customServerOrder = null;
        } else {
            val builder = ImmutableMap.<String, Integer>builder();
            int rank = 0;
            for (String server : customServerOrder) {
                builder.put(server, rank++);
            }
            this.customServerOrder = builder.build();
        }
    }

    @Override
    public ComponentTemplate toTemplate(TemplateCreationContext tcc) {
        if (ConfigValidationUtil.checkNotNull(tcc, "!players_by_server component", "playerSet", playerSet, getStartMark())) {
            if (!tcc.getPlayerSets().containsKey(playerSet.getValue())) {
                tcc.getErrorHandler().addError("No player set definition available for player set \"" + playerSet.getValue() + "\"", playerSet.getStartMark());
            }
        }

        PlayerOrderTemplate playerOrderTemplate = PlayerOrderConfiguration.DEFAULT.toTemplate(tcc);
        if (ConfigValidationUtil.checkNotNull(tcc, "!players_by_server component", "playerOrder", playerOrder, getStartMark())) {
            playerOrderTemplate = this.playerOrder.toTemplate(tcc);

        }
        if (minSize.getValue() < 0) {
            tcc.getErrorHandler().addError("Failed to configure players component. MinSize is negative", minSize.getStartMark());
        }
        if (maxSize.getValue() != -1 && minSize.getValue() > maxSize.getValue()) {
            tcc.getErrorHandler().addError("Failed to configure players component. MaxSize is lower than minSize", maxSize.getStartMark());
        }

        if (minSizePerServer.getValue() < 0) {
            tcc.getErrorHandler().addError("Failed to configure players component. MinSizePerWorld is negative", minSizePerServer.getStartMark());
        }
        if (maxSizePerServer.getValue() != -1 && minSizePerServer.getValue() > maxSizePerServer.getValue()) {
            tcc.getErrorHandler().addError("Failed to configure players component. MaxSizePerWorld is lower than minSizePerWorld", maxSizePerServer.getStartMark());
        }


        TemplateCreationContext childContextS = tcc.clone();
        if (fillSlotsVertical) {
            childContextS.setColumns(1);
        }
        BungeeTabListPlus btlp = BungeeTabListPlus.getInstance();
        childContextS.addPlaceholderResolver(new ComponentServerPlaceholderResolver(btlp.getServerPlaceholderResolver(), btlp.getDataManager()));

        TemplateCreationContext childContextP = childContextS.clone();
        childContextP.setDefaultIcon(new PlayerIconTemplate(PlayerPlaceholderResolver.BindPoint.PLAYER, tcc.getPlayerIconDataKey()));
        childContextP.setDefaultPing(new PlayerPingTemplate(PlayerPlaceholderResolver.BindPoint.PLAYER, tcc.getPlayerPingDataKey()));
        childContextP.setPlayerAvailable(true);

        TemplateCreationContext childContextM = tcc.clone();
        if (fillSlotsVertical) {
            childContextM.setColumns(1);
        }
        childContextM.addPlaceholderResolver(new OtherCountPlaceholderResolver());

        ComponentTemplate playerComponentTemplate = tcc.emptyComponent(); // dummy
        if (ConfigValidationUtil.checkNotNull(tcc, "!players_by_server component", "playerComponent", playerComponent, getStartMark())) {
            playerComponentTemplate = this.playerComponent.toTemplate(childContextP);
            ComponentTemplate.LayoutInfo layoutInfo = playerComponentTemplate.getLayoutInfo();
            if (!layoutInfo.isConstantSize()) {
                tcc.getErrorHandler().addError("Failed to configure !players_by_server component. Attribute playerComponent must not have variable size.", playerComponent.getStartMark());
            }
            if (layoutInfo.isBlockAligned()) {
                tcc.getErrorHandler().addError("Failed to configure !players_by_server component. Attribute playerComponent must not require block alignment.", playerComponent.getStartMark());
            }
        }

        if (!ConfigValidationUtil.checkNotNull(tcc, "!players_by_server component", "hiddenServers", hiddenServers, getStartMark())) {
            hiddenServers = new MarkedListProperty<>();
        }

        if (includeEmptyServers != null) {
            if (includeEmptyServers.isValue()) {
                tcc.getErrorHandler().addWarning("'includeEmptyServers: true' is deprecated and will be removed. Use 'showServers: ALL' instead.", includeEmptyServers.getStartMark());
            } else {
                tcc.getErrorHandler().addWarning("'includeEmptyServers: false' is deprecated and will be removed. Use 'showServers: NON_EMPTY' instead.", includeEmptyServers.getStartMark());
            }
        }

        if (showServers == null) {
            if (includeEmptyServers != null) {
                showServers = includeEmptyServers.isValue() ? ServerOptions.ALL : ServerOptions.NON_EMPTY;
            } else {
                showServers = ServerOptions.ALL;
            }
        }

        ContextAwareOrdering<Context, PlayerSetPartition, String> serverComparator = null;

        if (serverOrder != null) {
            List<ContextAwareOrdering<Context, PlayerSetPartition, String>> list = Stream.of(serverOrder.getValue().split(","))
                    .filter((s1) -> !s1.isEmpty())
                    .map(String::toLowerCase)
                    .map((String rule) -> getServerComparator(rule, tcc, serverOrder.getStartMark()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                serverComparator = ContextAwareOrdering.compound(list);
            }
        }

        ComponentTemplate morePlayersComponentTemplate;
        if (this.morePlayersComponent != null) {

            morePlayersComponentTemplate = this.morePlayersComponent.toTemplate(childContextM);
            ComponentTemplate.LayoutInfo layoutInfo = morePlayersComponentTemplate.getLayoutInfo();
            if (!layoutInfo.isConstantSize()) {
                tcc.getErrorHandler().addError("Failed to configure !players_by_server component. Attribute playerComponent cannot have variable size.", morePlayersComponent.getStartMark());
            }
            if (layoutInfo.isBlockAligned()) {
                tcc.getErrorHandler().addError("Failed to configure !players_by_server component. Attribute playerComponent must not require block alignment.", morePlayersComponent.getStartMark());
            }
        } else {
            morePlayersComponentTemplate = childContextM.emptyComponent();
        }

        Map<String, String> serverMap = new HashMap<>();
        if (mergeServers != null) {
            for (Map.Entry<String, List<String>> entry : mergeServers.entrySet()) {
                String groupName = entry.getKey();
                List<String> serverNames = entry.getValue();
                if (serverNames != null) {
                    for (String serverName : serverNames) {
                        serverMap.put(serverName, groupName);
                    }
                }
            }
        }

        return PlayersByServerComponentTemplate.builder()
                .playerOrder(playerOrderTemplate)
                .playerSet(tcc.getPlayerSets().get(playerSet.getValue()))
                .playerComponent(playerComponentTemplate)
                .morePlayersComponent(morePlayersComponentTemplate)
                .serverHeader(serverHeader != null ? serverHeader.toTemplate(childContextS) : null)
                .serverFooter(serverFooter != null ? serverFooter.toTemplate(childContextS) : null)
                .serverSeparator(serverSeparator != null ? serverSeparator.toTemplate(tcc) : null)
                .fillSlotsVertical(fillSlotsVertical)
                .minSize(minSize.getValue())
                .maxSize(maxSize.getValue())
                .minSizePerServer(minSizePerServer.getValue())
                .maxSizePerServer(maxSizePerServer.getValue())
                .columns(tcc.getColumns().orElse(1))
                .defaultIcon(tcc.getDefaultIcon())
                .defaultText(tcc.getDefaultText())
                .defaultPing(tcc.getDefaultPing())
                .partitionFunction(tcc.getExpressionEngine().compile(childContextP, "${player server}", null))
                .mergeSections(new MapFunction(serverMap))
                .hiddenServers(ImmutableSet.copyOf(hiddenServers))
                .showServers(showServers)
                .serverComparator(serverComparator)
                .prioritizeViewerServer(prioritizeViewerServer)
                .build();
    }

    private ContextAwareOrdering<Context, PlayerSetPartition, String> getServerComparator(String rule, TemplateCreationContext tcc, Mark mark) {
        switch (rule) {
            case "alphabetically":
                return ContextAwareOrdering.from(Comparator.<String>naturalOrder());
            case "playercount":
                return new ContextAwareOrdering<Context, PlayerSetPartition, String>() {
                    @Override
                    public int compare(Context context, PlayerSetPartition partition, String first, String second) {
                        PlayerSet ps1 = partition.getPartition(first);
                        PlayerSet ps2 = partition.getPartition(second);
                        int pc1 = ps1 != null ? ps1.getCount() : 0;
                        int pc2 = ps2 != null ? ps2.getCount() : 0;
                        return -Integer.compare(pc1, pc2);
                    }
                };
            case "online":
                return ContextAwareOrdering.from(Comparator.comparing(server -> BungeeTabListPlus.getInstance().getDataManager().getServerDataHolder(server).get(BTLPBungeeDataKeys.DATA_KEY_SERVER_ONLINE), Comparator.reverseOrder()));
            case "custom":
                if (customServerOrder == null) {
                    tcc.getErrorHandler().addWarning("Selected serverOrder option 'custom', but 'customServerOrder' option is not set.", serverOrder.getStartMark());
                }
                Map<String, Integer> order = customServerOrder != null ? customServerOrder : Collections.emptyMap();
                return ContextAwareOrdering.from(Comparator.comparing(server -> order.getOrDefault(server, Integer.MAX_VALUE)));
            case "yourserverfirst":
                return new ContextAwareOrdering<Context, PlayerSetPartition, String>() {
                    @Override
                    public int compare(Context context, PlayerSetPartition partition, String first, String second) {
                        String server = context.getViewer().get(BungeeData.BungeeCord_Server);
                        if (first.equals(server)) {
                            return -1;
                        } else if (second.equals(server)) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                };
            default:
                tcc.getErrorHandler().addWarning("Unknown serverOrder option: '" + rule + "'", mark);
                return null;
        }
    }

    public enum ServerOptions {
        ALL, ONLINE, NON_EMPTY
    }

}
