package codecrafter47.bungeetablistplus.view;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.config.PlayersByServerComponentConfiguration;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.util.ContextAwareOrdering;
import codecrafter47.bungeetablistplus.util.EmptyPlayerSet;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.expression.template.ExpressionTemplate;
import de.codecrafter47.taboverlay.config.player.PlayerSet;
import de.codecrafter47.taboverlay.config.player.PlayerSetPartition;
import de.codecrafter47.taboverlay.config.template.PlayerOrderTemplate;
import de.codecrafter47.taboverlay.config.template.PlayerSetTemplate;
import de.codecrafter47.taboverlay.config.template.component.ComponentTemplate;
import de.codecrafter47.taboverlay.config.template.icon.IconTemplate;
import de.codecrafter47.taboverlay.config.template.ping.PingTemplate;
import de.codecrafter47.taboverlay.config.template.text.TextTemplate;
import de.codecrafter47.taboverlay.config.view.components.ComponentView;
import de.codecrafter47.taboverlay.config.view.components.ContainerComponentView;
import de.codecrafter47.taboverlay.config.view.components.ListComponentView;
import de.codecrafter47.taboverlay.config.view.components.PartitionedPlayersView;
import net.md_5.bungee.api.ProxyServer;

import java.util.*;

public class PlayersByServerComponentView extends PartitionedPlayersView {

    private final List<String> hiddenServers;
    private final PlayersByServerComponentConfiguration.ServerOptions showServers;
    private final ContextAwareOrdering<Context, PlayerSetPartition, String> serverComparator;

    private final Set<String> persistentSections = new HashSet<>();
    protected final Map<String, ComponentView> emptySectionMap = new HashMap<>();

    private final Runnable onlineStateUpdateListener = this::updateOnlineServers;
    private List<String> servers;

    public PlayersByServerComponentView(int columns, PlayerSetTemplate playerSetTemplate, ComponentTemplate playerComponentTemplate, int playerComponentSize, ComponentTemplate morePlayerComponentTemplate, int morePlayerComponentSize, PlayerOrderTemplate playerOrderTemplate, TextTemplate defaultTextTemplate, PingTemplate defaultPingTemplate, IconTemplate defaultIconTemplate, ExpressionTemplate partitionFunction, ComponentTemplate sectionHeader, ComponentTemplate sectionFooter, ComponentTemplate sectionSeparator, int minSizePerSection, int maxSizePerSection, SectionContextFactory sectionContextFactory, List<String> hiddenServers, PlayersByServerComponentConfiguration.ServerOptions showServers, ContextAwareOrdering<Context, PlayerSetPartition, String> serverComparator) {
        super(columns, playerSetTemplate, playerComponentTemplate, playerComponentSize, morePlayerComponentTemplate, morePlayerComponentSize, playerOrderTemplate, defaultTextTemplate, defaultPingTemplate, defaultIconTemplate, partitionFunction, sectionHeader, sectionFooter, sectionSeparator, minSizePerSection, maxSizePerSection, sectionContextFactory);
        this.hiddenServers = hiddenServers;
        this.showServers = showServers;
        this.serverComparator = serverComparator;
    }

    @Override
    protected void onActivation() {
        super.onActivation();

        if (showServers == PlayersByServerComponentConfiguration.ServerOptions.ALL) {
            servers = new ArrayList<>(ProxyServer.getInstance().getServers().keySet());
            for (String serverName : servers) {
                addPersistentSection(serverName, false);
            }
        } else if (showServers == PlayersByServerComponentConfiguration.ServerOptions.ONLINE) {
            servers = new ArrayList<>(ProxyServer.getInstance().getServers().keySet());
            for (String serverName : servers) {
                DataHolder serverDataHolder = BungeeTabListPlus.getInstance().getDataManager().getServerDataHolder(serverName);
                serverDataHolder.addDataChangeListener(BTLPBungeeDataKeys.DATA_KEY_SERVER_ONLINE, onlineStateUpdateListener);
                if (serverDataHolder.get(BTLPBungeeDataKeys.DATA_KEY_SERVER_ONLINE)) {
                    addPersistentSection(serverName, false);
                }
            }
        }
    }

    @Override
    protected void onDeactivation() {
        super.onDeactivation();
        if (showServers == PlayersByServerComponentConfiguration.ServerOptions.ONLINE) {
            for (String serverName : servers) {
                DataHolder serverDataHolder = BungeeTabListPlus.getInstance().getDataManager().getServerDataHolder(serverName);
                serverDataHolder.removeDataChangeListener(BTLPBungeeDataKeys.DATA_KEY_SERVER_ONLINE, onlineStateUpdateListener);
            }
        }
    }

    private void updateOnlineServers() {
        for (String serverName : servers) {
            DataHolder serverDataHolder = BungeeTabListPlus.getInstance().getDataManager().getServerDataHolder(serverName);
            serverDataHolder.addDataChangeListener(BTLPBungeeDataKeys.DATA_KEY_SERVER_ONLINE, onlineStateUpdateListener);
            Boolean online = serverDataHolder.get(BTLPBungeeDataKeys.DATA_KEY_SERVER_ONLINE);
            if (online == null) {
                online = true;
            }
            if (online && !persistentSections.contains(serverName)) {
                addPersistentSection(serverName, true);
            } else if (!online && persistentSections.contains(serverName)) {
                removePersistentSection(serverName, true);
            }
        }
    }

    private void addPersistentSection(String id, boolean notify) {
        persistentSections.add(id);
        if (!sectionMap.containsKey(id)) {
            addEmptySection(id, notify);
        }
    }

    private void removePersistentSection(String id, boolean notify) {
        persistentSections.remove(id);
        if (persistentSections.contains(id)) {
            removeEmptySection(id, notify);
        }
    }

    private void addEmptySection(String id, boolean notify) {
        Context sectionContext = sectionContextFactory.createSectionContext(getContext(), id, EmptyPlayerSet.INSTANCE);
        ComponentView componentView = createEmptySectionView(id, sectionContext);
        componentView.activate(sectionContext, this);
        emptySectionMap.put(id, componentView);
        if (sectionSeparator != null && !super.components.isEmpty()) {
            ComponentView separator = sectionSeparator.instantiate();
            separator.activate(getContext(), this);
            super.components.add(separator);
        }
        super.components.add(componentView);
        updateLayoutRequirements(notify);
    }

    private void removeEmptySection(String id, boolean notify) {
        ComponentView componentView = emptySectionMap.remove(id);
        int index = super.components.indexOf(componentView);
        if (sectionSeparator == null) {
            super.components.remove(index);
        } else if (index != 0 && super.components.size() > 1) {
            super.components.remove(index);
            ComponentView separator = super.components.remove(index - 1);
            separator.deactivate();
        } else if (index != super.components.size() - 1 && super.components.size() > 1) {
            super.components.remove(index);
            ComponentView separator = super.components.remove(index);
            separator.deactivate();
        } else {
            super.components.remove(index);
        }
        componentView.deactivate();
        updateLayoutRequirements(notify);
    }

    private ComponentView createEmptySectionView(String id, Context sectionContext) {
        List<ComponentView> components = new ArrayList<>();
        if (sectionHeader != null) {
            components.add(sectionHeader.instantiate());
        }
        if (sectionFooter != null) {
            components.add(sectionFooter.instantiate());
        }
        return new ContainerComponentView(new ListComponentView(components, super.columns, defaultTextTemplate.instantiate(), defaultPingTemplate.instantiate(), defaultIconTemplate.instantiate()), false, minSizePerSection, maxSizePerSection, super.columns, true);
    }

    @Override
    protected void addPartition(String id, PlayerSet playerSet, boolean notify) {
        if (id == null || hiddenServers.contains(id)) {
            return;
        }
        if (persistentSections.contains(id)) {
            removeEmptySection(id, false);
        }
        super.addPartition(id, playerSet, notify);
    }

    @Override
    public void onPartitionRemoved(String id) {
        if (id == null || hiddenServers.contains(id)) {
            return;
        }
        super.onPartitionRemoved(id);
        if (persistentSections.contains(id)) {
            addEmptySection(id, true);
        }
    }

    @Override
    protected void updateLayoutRequirements(boolean notify) {
        if (serverComparator != null) {
            List<String> serverNames = new ArrayList<>();
            serverNames.addAll(sectionMap.keySet());
            serverNames.addAll(emptySectionMap.keySet());
            List<String> sortedServers = serverComparator.immutableSortedCopy(getContext(), playerSetPartition, serverNames);

            List<ComponentView> elements = new ArrayList<>();
            for (String server : sortedServers) {
                if (sectionMap.containsKey(server)) {
                    elements.add(sectionMap.get(server));
                } else {
                    elements.add(emptySectionMap.get(server));
                }
            }

            if (sectionSeparator == null) {
                for (int i = 0; i < elements.size(); i++) {
                    ComponentView element = elements.get(i);
                    super.components.set(i, element);
                }
            } else {
                for (int i = 0; i < elements.size(); i++) {
                    ComponentView element = elements.get(i);
                    super.components.set(2 * i, element);
                }
            }
        }
        super.updateLayoutRequirements(notify);
    }
}
