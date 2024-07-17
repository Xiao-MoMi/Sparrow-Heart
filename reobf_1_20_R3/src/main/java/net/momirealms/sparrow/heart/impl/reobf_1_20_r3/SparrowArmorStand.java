package net.momirealms.sparrow.heart.impl.reobf_1_20_r3;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec3;
import net.momirealms.sparrow.heart.feature.armorstand.FakeArmorStand;
import net.momirealms.sparrow.heart.util.SelfIncreaseEntityID;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SparrowArmorStand implements FakeArmorStand {

    private final Location location;
    private boolean small = false;
    private boolean invisible = false;
    private String name;
    private final List<Pair<EquipmentSlot, net.minecraft.world.item.ItemStack>> equipments = new ArrayList<>();
    private final int entityID = SelfIncreaseEntityID.getAndIncrease();
    private final UUID uuid = UUID.randomUUID();

    public SparrowArmorStand(Location location) {
        this.location = location;
    }

    @Override
    public void small(boolean small) {
        this.small = small;
    }

    @Override
    public void invisible(boolean invisible) {
        this.invisible = invisible;
    }

    @Override
    public void name(String json) {
        this.name = json;
    }

    @Override
    public void equipment(org.bukkit.inventory.EquipmentSlot slot, org.bukkit.inventory.ItemStack itemStack) {
        this.equipments.add(Pair.of(CraftEquipmentSlot.getNMS(slot), CraftItemStack.asNMSCopy(itemStack)));
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
                EntityType.ARMOR_STAND, 0,
                Vec3.ZERO, 0
        );
        ArrayList<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        if (invisible) {
            values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), (byte) (0x20)));
        }
        if (name != null) {
            values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(3, EntityDataSerializers.BOOLEAN), true));
            values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT), Optional.of(CraftChatMessage.fromJSON(name))));
        }
        if (small) {
            values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(15, EntityDataSerializers.BYTE), (byte) 0x01));
        }
        ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entityID, values);
        ArrayList<Packet<ClientGamePacketListener>> packets = new ArrayList<>(List.of(entityPacket, dataPacket));
        if (!equipments.isEmpty()) {
            ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(entityID, equipments);
            packets.add(equipmentPacket);
        }
        ClientboundBundlePacket packet = new ClientboundBundlePacket(packets);
        serverPlayer.connection.send(packet);
    }

    @Override
    public int entityID() {
        return entityID;
    }
}