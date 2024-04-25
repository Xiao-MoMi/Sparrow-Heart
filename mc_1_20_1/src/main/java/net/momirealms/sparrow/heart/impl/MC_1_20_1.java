package net.momirealms.sparrow.heart.impl;

import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.momirealms.sparrow.heart.heart.SparrowHeart;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MC_1_20_1 extends SparrowHeart {

    @Override
    public void sendActionBar(Player player, String json) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer serverPlayer = craftPlayer.getHandle();
        ClientboundSetActionBarTextPacket packet = new ClientboundSetActionBarTextPacket(Objects.requireNonNull(Component.Serializer.fromJson(json)));
        serverPlayer.connection.send(packet);
    }

    @Override
    public void sendTitle(Player player, @Nullable String titleJson, @Nullable String subTitleJson, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        ServerPlayer serverPlayer = craftPlayer.getHandle();
        ArrayList<Packet<ClientGamePacketListener>> packetListeners = new ArrayList<>();
        packetListeners.add(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
        if (titleJson != null) {
            packetListeners.add(new ClientboundSetTitleTextPacket(Objects.requireNonNull(Component.Serializer.fromJson(titleJson))));
        } else {
            packetListeners.add(new ClientboundSetTitleTextPacket(Objects.requireNonNull(Component.empty())));
        }
        if (subTitleJson != null) {
            packetListeners.add(new ClientboundSetSubtitleTextPacket(Objects.requireNonNull(Component.Serializer.fromJson(subTitleJson))));
        }
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packetListeners);
        serverPlayer.connection.send(bundlePacket);
    }

    @Override
    public void sendToast(Player player, ItemStack icon, String titleJson, String advancementType) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(icon);
        DisplayInfo displayInfo = new DisplayInfo(nmsStack, Objects.requireNonNull(Component.Serializer.fromJson(titleJson)), Component.literal(""), null, FrameType.valueOf(advancementType), true, false, true);
        AdvancementRewards advancementRewards = AdvancementRewards.EMPTY;
        ResourceLocation id = new ResourceLocation("sparrow", "toast");
        Criterion criterion = new Criterion(new ImpossibleTrigger.TriggerInstance());
        HashMap<String, Criterion> criteria = new HashMap<>(Map.of("impossible", criterion));
        String[][] requirements = {{"impossible"}};
        Advancement advancement = new Advancement(id, null, displayInfo, advancementRewards, criteria, requirements, false);
        Map<ResourceLocation, AdvancementProgress> advancementsToGrant = new HashMap<>();
        AdvancementProgress advancementProgress = new AdvancementProgress();
        advancementProgress.update(criteria, requirements);
        Objects.requireNonNull(advancementProgress.getCriterion("impossible")).grant();
        advancementsToGrant.put(id, advancementProgress);
        ClientboundUpdateAdvancementsPacket packet1 = new ClientboundUpdateAdvancementsPacket(false, new ArrayList<>(List.of(advancement)), new HashSet<>(), advancementsToGrant);
        serverPlayer.connection.send(packet1);
        ClientboundUpdateAdvancementsPacket packet2 = new ClientboundUpdateAdvancementsPacket(false, new ArrayList<>(), new HashSet<>(List.of(id)), new HashMap<>());
        serverPlayer.connection.send(packet2);
    }
}
