package net.momirealms.sparrow.heart;

import net.momirealms.sparrow.heart.exception.UnsupportedVersionException;
import net.momirealms.sparrow.heart.feature.armorstand.FakeArmorStand;
import net.momirealms.sparrow.heart.feature.bossbar.BossBarColor;
import net.momirealms.sparrow.heart.feature.bossbar.BossBarOverlay;
import net.momirealms.sparrow.heart.feature.color.NamedTextColor;
import net.momirealms.sparrow.heart.feature.highlight.HighlightBlocks;
import net.momirealms.sparrow.heart.feature.inventory.HandSlot;
import net.momirealms.sparrow.heart.feature.team.TeamCollisionRule;
import net.momirealms.sparrow.heart.feature.team.TeamColor;
import net.momirealms.sparrow.heart.feature.team.TeamVisibility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.UUID;

public abstract class SparrowHeart {

    public static SparrowHeart getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final SparrowHeart INSTANCE = getHeart();

        public static SparrowHeart getHeart() {
            String bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
            String packageName;
            if (isMojMap()) {
                switch (bukkitVersion) {
                    case "1.20.5", "1.20.6" -> packageName = "mojmap_r1";
                    default -> throw new UnsupportedVersionException();
                }
            } else {
                switch (bukkitVersion) {
                    case "1.20.5", "1.20.6" -> packageName = "reobf_1_20_r4";
                    case "1.20.3", "1.20.4" -> packageName = "reobf_1_20_r3";
                    case "1.20.2" -> packageName = "reobf_1_20_r2";
                    case "1.20", "1.20.1" -> packageName = "reobf_1_20_r1";
                    case "1.19.4" -> packageName = "reobf_1_19_r3";
                    case "1.19.3" -> packageName = "reobf_1_19_r2";
                    case "1.19.2", "1.19.1" -> packageName = "reobf_1_19_r1";
                    case "1.18.2" -> packageName = "reobf_1_18_R2";
                    case "1.18.1", "1.18" -> packageName = "reobf_1_18_r1";
                    case "1.17.1" -> packageName = "reobf_1_17_r1";
                    default -> throw new UnsupportedVersionException();
                }
            }
            try {
                Class<?> clazz = Class.forName("net.momirealms.sparrow.heart.impl." + packageName + ".Heart");
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (SparrowHeart) constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialize sparrow heart", e);
            }
        }

        private static boolean isMojMap() {
            try {
                Class.forName("net.minecraft.world.level.biome.Biome");
                return true;
            } catch (ClassNotFoundException ignore) {
                return false;
            }
        }
    }

    public abstract void sendDebugMarker(Player player, Location location, String message, int duration, int color);

    public abstract String getBiomeResourceLocation(Location location);

    public abstract void sendActionBar(Player player, String json);

    public abstract void sendTitle(Player player, String titleJson, String subTitleJson, int fadeInTicks, int stayTicks, int fadeOutTicks);

    public abstract void sendToast(Player player, ItemStack icon, String titleJson, String advancementType);

    public abstract void sendDemo(Player player);

    public abstract void sendCredits(Player player);

    public abstract void sendTotemAnimation(Player player, ItemStack totem);

    public abstract void openCustomInventory(Player player, Inventory inventory, String jsonTitle);

    public abstract void updateInventoryTitle(Player player, String jsonTitle);

    public abstract void swingHand(Player player, HandSlot slot);

    public abstract EnchantmentOffer[] getEnchantmentOffers(Player player, ItemStack itemToEnchant, int shelves);

    public abstract HighlightBlocks highlightBlocks(Player player, NamedTextColor color, Location... locations);

    public abstract void removeClientSideTeam(Player player, String teamName);

    public abstract void addClientSideTeam(Player player, String teamName, List<String> members, String display, String prefix, String suffix, TeamVisibility tagVisibility, TeamVisibility messageVisibility, TeamCollisionRule collisionRule, TeamColor color, boolean allowFriendlyFire, boolean seeFriendlyInvisibles);

    public abstract void updateClientSideTeam(Player player, String teamName, String display, String prefix, String suffix, TeamVisibility tagVisibility, TeamVisibility messageVisibility, TeamCollisionRule collisionRule, TeamColor color, boolean allowFriendlyFire, boolean seeFriendlyInvisibles);

    public abstract void removeClientSideEntity(Player player, int... entityIDs);

    public abstract void sendClientSideTeleportEntity(Player player, Location location, boolean onGround, int... entityIDs);

    public abstract FakeArmorStand createFakeArmorStand(Location location);

    public abstract void createBossBar(Player player, UUID uuid, String displayName, BossBarColor color, BossBarOverlay overlay, float progress, boolean createWorldFog, boolean playBossMusic, boolean darkenScreen);

    public abstract void removeBossBar(Player player, UUID uuid);

    public abstract void updateBossBarName(Player player, UUID uuid, String displayName);

    public abstract void updateBossBarProgress(Player player, UUID uuid, float progress);

}
