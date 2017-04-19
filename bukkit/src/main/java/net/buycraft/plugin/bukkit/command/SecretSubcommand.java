package net.buycraft.plugin.bukkit.command;

import lombok.RequiredArgsConstructor;
import net.buycraft.plugin.bukkit.BuycraftPlugin;
import net.buycraft.plugin.client.ApiClient;
import net.buycraft.plugin.client.ProductionApiClient;
import net.buycraft.plugin.data.responses.ServerInformation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.logging.Level;

@RequiredArgsConstructor
public class SecretSubcommand implements Subcommand {
    private final BuycraftPlugin plugin;

    @Override
    public void execute(final CommandSender sender, final String[] args) {

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + plugin.getI18n().get("secret_need_key"));
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ApiClient client = new ProductionApiClient(args[0], plugin.getHttpClient());
                try {
                    plugin.updateInformation(client);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Unable to verify secret", e);
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("secret_does_not_work"));
                    return;
                }

                ServerInformation information = plugin.getServerInformation();
                plugin.setApiClient(client);
                plugin.getListingUpdateTask().run();
                plugin.getCouponUpdateTask().run();
                plugin.getConfiguration().setServerKey(args[0]);
                try {
                    plugin.saveConfiguration();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + plugin.getI18n().get("secret_cant_be_saved"));
                }

                sender.sendMessage(ChatColor.GREEN + plugin.getI18n().get("secret_success",
                        information.getServer().getName(), information.getAccount().getName()));

                plugin.getDuePlayerFetcher().run(false);
            }
        });
    }

    @Override
    public String getDescription() {
        return plugin.getI18n().get("usage_secret");
    }
}
