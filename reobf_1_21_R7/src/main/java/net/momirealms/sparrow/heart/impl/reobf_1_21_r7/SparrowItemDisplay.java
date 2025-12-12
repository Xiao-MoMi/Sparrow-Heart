package net.momirealms.sparrow.heart.impl.reobf_1_21_r7;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.momirealms.sparrow.heart.feature.entity.display.FakeItemDisplay;
import net.momirealms.sparrow.heart.util.SelfIncreaseEntityID;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SparrowItemDisplay implements FakeItemDisplay {

    private final Location location;
    private ItemStack item;
    private final int entityID = SelfIncreaseEntityID.getAndIncrease();
    private final UUID uuid = UUID.randomUUID();

    public SparrowItemDisplay(Location location) {
        this.location = location;
    }

    @Override
    public void destroy(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityID);
        serverPlayer.connection.send(packet);
    }

    @Override
    public void spawn(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundAddEntityPacket entityPacket = new ClientboundAddEntityPacket(
                entityID, uuid,
                location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw(),
                EntityType.ITEM_DISPLAY, 0,
                Vec3.ZERO, 0
        );

        ArrayList<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>(List.of(entityPacket, getMetaPacket()));
        ClientboundBundlePacket packet = new ClientboundBundlePacket(packets);
        serverPlayer.connection.send(packet);
    }

    @Override
    public void updateMetaData(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(getMetaPacket());
    }

    private ClientboundSetEntityDataPacket getMetaPacket() {
        ArrayList<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(23, EntityDataSerializers.ITEM_STACK), CraftItemStack.asNMSCopy(item)));
        return new ClientboundSetEntityDataPacket(entityID, values);
    }

    @Override
    public void item(ItemStack itemStack) {
        this.item = itemStack;
    }

    @Override
    public int entityID() {
        return entityID;
    }
}