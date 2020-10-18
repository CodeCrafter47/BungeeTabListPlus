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

package de.codecrafter47.bungeetablistplus.demo;

import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.api.bungee.Variable;
import de.codecrafter47.taboverlay.AbstractPlayerTabOverlayProvider;
import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.handler.*;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static de.codecrafter47.bungeetablistplus.demo.ColorUtil.getAWTColor;
import static de.codecrafter47.bungeetablistplus.demo.ColorUtil.getSimilarChatColor;
import static java.lang.Math.*;

public class DemoPlugin extends Plugin {

    private CompletableFuture<Icon> customIcon;

    @Override
    public void onLoad() {

        // variables should be registered in onLoad to avoid warnings when BTLP loads your config files
        BungeeTabListPlusAPI.registerVariable(this, new Variable("uppercase_name") {
            @Override
            public String getReplacement(ProxiedPlayer player) {
                return player.getName().toUpperCase();
            }
        });

        // Create our icon.
        try {
            // read the image file
            BufferedImage image = ImageIO.read(getResourceAsStream("icon.png"));
            // call getIconFromImage, this gives use a future that will hold the icon once completed
            customIcon = BungeeTabListPlusAPI.getIconFromImage(image);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to load icon.", ex);
        }
    }

    @Override
    public void onEnable() {

        // register the /tabdemo command.
        // It will display the custom tab list to players
        getProxy().getPluginManager().registerCommand(this, new Command("tabdemo") {
            @Override
            public void execute(CommandSender sender, String[] args) {
                if (sender instanceof ProxiedPlayer) {
                    // get the tab view for the player
                    TabView tabView = BungeeTabListPlusAPI.getTabViewForPlayer((ProxiedPlayer) sender);
                    // create a new instance of our CustomTabOverlayProvider and add it to the tab view
                    tabView.getTabOverlayProviders().addProvider(new CustomTabOverlayProvider(tabView));
                }
            }
        });
    }

    /**
     * Our custom tab overlay provider.
     */
    private class CustomTabOverlayProvider extends AbstractPlayerTabOverlayProvider {

        // In this field we will store the handle to access the header and footer
        private HeaderAndFooterHandle headerFooterHandle;
        // In the contentHandle field we store the handle to modify the content of the tab list
        private RectangularTabOverlay contentHandle;
        // The updateTask field stores the task hande for the update task
        private ScheduledTask updateTask;

        public CustomTabOverlayProvider(@Nonnull @NonNull TabView tabView) {
            // set the name to "custom-taboverlay-example", you can use any name, but it needs to be unique
            // set the priority to 1000. The plugin will display the tab overlay with the highest priority. So using a
            // high number here is good. It ensures our custom tab overlay is displayed and not a tab list provided by
            // a config file. The priority should be between 0 and 10000.
            super(tabView, "custom-taboverlay-example", 1000);
        }

        @Override
        protected void onAttach() {
            // This informs us that the tab overlay provider has been added to a tab view.
            // We can access the tab view using super.getTabView(). After this has been called BungeeTabListPlus
            // expects shouldActivate() to return correct values.

            // In this example we don't need to do anything here. However if you create a more complex tab overlay
            // provider, that isn't always active, but should activate depending on some condition, you might want to
            // do some stuff here.
        }

        @Override
        protected void onActivate(TabOverlayHandler handler) {
            // Our tab overlay provider has been activated

            // Configure that tab overlay of the player and store the handles so we can modify it later
            // IMPORTANT: Do not store a copy of handler anywhere!!!

            // for the header and footer we can choose either custom or pass through
            // we choose custom
            headerFooterHandle = handler.enterHeaderAndFooterOperationMode(HeaderAndFooterOperationMode.CUSTOM);

            // for the content we can choose between rectangular, simple and pass through.
            // we choose rectangular
            contentHandle = handler.enterContentOperationMode(ContentOperationMode.RECTANGULAR);

            // We set the header text
            headerFooterHandle.setHeader("&6Super &eAwesome &cClock");

            // now we set the size of the content to 1 column, 19 rows
            contentHandle.setSize(new RectangularTabOverlay.Dimension(1, 19));

            // we schedule a task to update the content every second
            // we store the task handle in a field so we can cancel it later
            updateTask = getProxy().getScheduler().schedule(DemoPlugin.this, this::updateContent, 0, 1, TimeUnit.SECONDS);
        }

        @Override
        protected void onDeactivate() {
            // Our tab overlay provider has been deactivated
            // We should now stop modifying the tab list

            // We cancel the update task
            updateTask.cancel();
        }

        @Override
        protected void onDetach() {
            // Our tab overlay provider has been detached from the tab view.
            // This means it is no longer used at all.

            // You can use this method to free any resources you might have acquired.

            // In this example we don't have to do anything here.
        }

        @Override
        protected boolean shouldActivate() {
            // This method is used by the plugin to check whether this tab overlay provider should be active.

            // In our example we want our custom tab overlay provider to always be used so we always return true.
            return true;
        }

        // This method renders an analogue clock to the tab list.
        private void updateContent() {
            // First we draw to a buffered image
            BufferedImage image = renderClock();

            // now we convert the image to text lines and set the appropriate slot of the tab list
            for (int row = 0; row < 19; row++) {
                String text = "";
                for (int x = 0; x < 19; x++) {
                    ChatColor chatColor = getSimilarChatColor(new Color(image.getRGB(x, row)));
                    text += chatColor == null ? ' ' : chatColor.toString() + '█';
                }

                // get our custom icon. If it's not ready yet use the default alex icon
                Icon icon = customIcon.getNow(Icon.DEFAULT_ALEX);

                // set the icon, text and ping of the slot
                contentHandle.setSlot(0, row, icon, text, 0);
            }
        }
    }

    // This method renders an analogue clock to a buffered image.
    @Nonnull
    private BufferedImage renderClock() {
        BufferedImage image = new BufferedImage(19, 19, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        // background
        g.setColor(getAWTColor(ChatColor.DARK_GRAY));
        g.fillRect(0, 0, 19, 19);
        // circle
        g.setColor(getAWTColor(ChatColor.GRAY));
        for (int x = 0; x < 19; x++)
            for (int y = 0; y < 19; y++)
                if ((8.5 - x) * (8.5 - x) + (8.5 - y) * (8.5 - y) < 81)
                    g.drawRect(x, y, 1, 1);
        // arrows
        int hour = Calendar.getInstance().get(Calendar.HOUR);
        g.setColor(getAWTColor(ChatColor.DARK_RED));
        g.drawLine(9, 9, (int) round(9 + 8 * sin(hour / 6.0 * PI)), (int) round(9 - 8 * cos(hour / 6.0 * PI)));
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        g.setColor(getAWTColor(ChatColor.RED));
        g.drawLine(9, 9, (int) round(9 + 8 * sin(minute / 30.0 * PI)), (int) round(9 - 8 * cos(minute / 30.0 * PI)));
        int second = Calendar.getInstance().get(Calendar.SECOND);
        g.setColor(getAWTColor(ChatColor.GOLD));
        g.drawLine(9, 9, (int) round(9 + 9 * sin(second / 30.0 * PI)), (int) round(9 - 9 * cos(second / 30.0 * PI)));
        return image;
    }
}
