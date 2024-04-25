package net.momirealms.sparrow.heart.heart.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.momirealms.sparrow.heart.heart.SparrowHeart;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class MC_1_20_4 extends SparrowHeart {

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
        }
        if (subTitleJson != null) {
            packetListeners.add(new ClientboundSetSubtitleTextPacket(Objects.requireNonNull(Component.Serializer.fromJson(subTitleJson))));
        }
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packetListeners);
        serverPlayer.connection.send(bundlePacket);
    }
}
