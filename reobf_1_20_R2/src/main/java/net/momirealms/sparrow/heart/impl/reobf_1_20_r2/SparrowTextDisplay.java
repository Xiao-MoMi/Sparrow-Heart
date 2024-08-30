package net.momirealms.sparrow.heart.impl.reobf_1_20_r2;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.momirealms.sparrow.heart.feature.entity.display.FakeTextDisplay;
import net.momirealms.sparrow.heart.util.ColorUtils;
import net.momirealms.sparrow.heart.util.SelfIncreaseEntityID;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SparrowTextDisplay implements FakeTextDisplay {

    private final Location location;
    private String name;
    private final int entityID = SelfIncreaseEntityID.getAndIncrease();
    private final UUID uuid = UUID.randomUUID();
    private int rgba;

    public SparrowTextDisplay(Location location) {
        this.location = location;
    }

    @Override
    public void rgba(int r, int g, int b, int a) {
        this.rgba = ColorUtils.rgbaToDecimal(r, g, b, a);
    }

    @Override
    public void name(String json) {
        this.name = json;
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
                EntityType.TEXT_DISPLAY, 0,
                Vec3.ZERO, 0
        );
        ArrayList<SynchedEntityData.DataValue<?>> values = new ArrayList<>();
        values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(15, EntityDataSerializers.BYTE), (byte) 3));
        values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(23, EntityDataSerializers.COMPONENT), CraftChatMessage.fromJSON(name)));
        values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(25, EntityDataSerializers.INT), rgba));
        values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(26, EntityDataSerializers.BYTE), (byte) -1));
        values.add(SynchedEntityData.DataValue.create(new EntityDataAccessor<>(27, EntityDataSerializers.BYTE), (byte) 0));
        ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entityID, values);
        ArrayList<Packet<ClientGamePacketListener>> packets = new ArrayList<>(List.of(entityPacket, dataPacket));
        ClientboundBundlePacket packet = new ClientboundBundlePacket(packets);
        serverPlayer.connection.send(packet);
    }

    @Override
    public int entityID() {
        return entityID;
    }
}