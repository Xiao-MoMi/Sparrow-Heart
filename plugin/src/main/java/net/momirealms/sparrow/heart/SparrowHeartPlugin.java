package net.momirealms.sparrow.heart;

import net.momirealms.sparrow.heart.argument.NamedTextColor;
import net.momirealms.sparrow.heart.feature.highlight.HighlightBlocks;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class SparrowHeartPlugin extends JavaPlugin implements TabExecutor {

    private SparrowHeart heart;

    @Override
    public void onLoad() {
        heart = SparrowHeart.getInstance();
    }

    @Override
    public void onEnable() {
        PluginCommand pluginCommand = Bukkit.getPluginCommand("sparrowheart");
        if (pluginCommand != null) {
            pluginCommand.setTabCompleter(this);
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;
        if (sender instanceof Player player) {
             HighlightBlocks blocks = heart.highlightBlocks(player, NamedTextColor.AQUA, player.getLocation());
             Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                blocks.destroy(player);
             }, 100);
        }
        return true;
    }
}
