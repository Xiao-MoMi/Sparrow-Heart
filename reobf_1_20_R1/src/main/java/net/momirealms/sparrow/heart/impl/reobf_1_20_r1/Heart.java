package net.momirealms.sparrow.heart.impl.reobf_1_20_r1;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.level.material.WaterFluid;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.armorstand.FakeArmorStand;
import net.momirealms.sparrow.heart.feature.bossbar.BossBarColor;
import net.momirealms.sparrow.heart.feature.bossbar.BossBarOverlay;
import net.momirealms.sparrow.heart.feature.color.NamedTextColor;
import net.momirealms.sparrow.heart.feature.display.FakeItemDisplay;
import net.momirealms.sparrow.heart.feature.display.FakeTextDisplay;
import net.momirealms.sparrow.heart.feature.fluid.FluidData;
import net.momirealms.sparrow.heart.feature.highlight.HighlightBlocks;
import net.momirealms.sparrow.heart.feature.inventory.HandSlot;
import net.momirealms.sparrow.heart.feature.team.TeamCollisionRule;
import net.momirealms.sparrow.heart.feature.team.TeamColor;
import net.momirealms.sparrow.heart.feature.team.TeamVisibility;
import net.momirealms.sparrow.heart.util.BossBarUtils;
import net.momirealms.sparrow.heart.util.SelfIncreaseEntityID;
import net.momirealms.sparrow.heart.util.SelfIncreaseInt;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftFishHook;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftVector;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Heart extends SparrowHeart {

    private final Registry<Biome> biomeRegistry = MinecraftServer.getServer().registries().compositeAccess().registryOrThrow(Registries.BIOME);
    private final Enum<?> addBossBarOperation;
    private final Enum<?> updateBossBarNameOperation;
    private final Enum<?> updateBossBarProgressOperation;
    private final EntityDataAccessor<Boolean> dataBiting;
    private final Method sendPacketImmediateMethod;

    public Heart() {
        try {
            Class<?> cls = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutBoss$d");
            Field fieldAdd = cls.getDeclaredField("a");
            fieldAdd.setAccessible(true);
            addBossBarOperation = (Enum<?>) fieldAdd.get(null);
            Field fieldUpdateProgress = cls.getDeclaredField("c");
            fieldUpdateProgress.setAccessible(true);
            updateBossBarProgressOperation = (Enum<?>) fieldUpdateProgress.get(null);
            Field fieldUpdateName = cls.getDeclaredField("d");
            fieldUpdateName.setAccessible(true);
            updateBossBarNameOperation = (Enum<?>) fieldUpdateName.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get add boss bar operation", e);
        }
        try {
            Field dataBitingField = FishingHook.class.getDeclaredField("h");
            dataBitingField.setAccessible(true);
            dataBiting = (EntityDataAccessor<Boolean>) dataBitingField.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get hook biting state", e);
        }
        try {
            sendPacketImmediateMethod = Connection.class.getDeclaredMethod("sendPacket", Packet.class, PacketSendListener.class, Boolean.class);
            sendPacketImmediateMethod.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get send packet method", e);
        }
        SparrowFluidData.register(LavaFluid.Source.class, SparrowFallingFluidData::new);
        SparrowFluidData.register(WaterFluid.Source.class, SparrowFallingFluidData::new);
        SparrowFluidData.register(LavaFluid.Flowing.class, SparrowFlowingFluidData::new);
        SparrowFluidData.register(WaterFluid.Flowing.class, SparrowFlowingFluidData::new);
    }

    private void sendPacketImmediately(ServerPlayer serverPlayer, Packet<ClientGamePacketListener> packet) {
        try {
            sendPacketImmediateMethod.invoke(serverPlayer.connection.connection, packet, null, true);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to send packet", e);
        }
    }

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
        sendPacketImmediately(serverPlayer, bundlePacket);
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
        ClientboundUpdateAdvancementsPacket packet2 = new ClientboundUpdateAdvancementsPacket(false, new ArrayList<>(), new HashSet<>(List.of(id)), new HashMap<>());
        ArrayList<Packet<ClientGamePacketListener>> packetListeners = new ArrayList<>();
        packetListeners.add(packet1);
        packetListeners.add(packet2);
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packetListeners);
        serverPlayer.connection.send(bundlePacket);
    }

    @Override
    public void sendDemo(Player player) {
        ClientboundGameEventPacket packet = new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 0);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void sendCredits(Player player) {
        ClientboundGameEventPacket packet = new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 1F);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void sendTotemAnimation(Player player, ItemStack totem) {
        ItemStack previousItem = player.getInventory().getItemInOffHand();
        ClientboundSetEquipmentPacket packet1 = new ClientboundSetEquipmentPacket(player.getEntityId(), List.of(Pair.of(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(totem))));
        ClientboundEntityEventPacket packet2 = new ClientboundEntityEventPacket(((CraftPlayer) player).getHandle(), (byte) 35);
        ClientboundSetEquipmentPacket packet3 = new ClientboundSetEquipmentPacket(player.getEntityId(), List.of(Pair.of(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(previousItem))));
        ArrayList<Packet<ClientGamePacketListener>> packetListeners = new ArrayList<>();
        packetListeners.add(packet1);
        packetListeners.add(packet2);
        packetListeners.add(packet3);
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packetListeners);
        ((CraftPlayer) player).getHandle().connection.send(bundlePacket);
    }

    @Override
    public void openCustomInventory(Player player, Inventory inventory, String jsonTitle) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        MenuType<?> menuType = CraftContainer.getNotchInventoryType(inventory);
        AbstractContainerMenu menu = new CraftContainer(inventory, serverPlayer, serverPlayer.nextContainerCounter());
        menu = CraftEventFactory.callInventoryOpenEvent(serverPlayer, menu);
        if (menu != null) {
            Component titleComponent = CraftChatMessage.fromJSON(jsonTitle);
            menu.checkReachable = false;
            serverPlayer.connection.send(new ClientboundOpenScreenPacket(menu.containerId, menuType, titleComponent));
            serverPlayer.containerMenu = menu;
            serverPlayer.initMenu(menu);
        }
    }

    @Override
    public void updateInventoryTitle(Player player, String jsonTitle) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        serverPlayer.connection.send(new ClientboundOpenScreenPacket(menu.containerId, menu.getType(), CraftChatMessage.fromJSON(jsonTitle)));
        serverPlayer.initMenu(menu);
    }

    @Override
    public void swingHand(Player player, HandSlot slot) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(serverPlayer, slot.getId());
        serverPlayer.connection.send(packet);
        ChunkMap.TrackedEntity tracker = serverPlayer.tracker;
        if (tracker != null) {
            for (ServerPlayerConnection connection : tracker.seenBy) {
                connection.send(packet);
            }
        }
    }

    @Override
    public EnchantmentOffer[] getEnchantmentOffers(Player player, ItemStack itemToEnchant, int shelves) {
        EnchantmentOffer[] offers = new EnchantmentOffer[3];
        RandomSource random = RandomSource.create();
        DataSlot enchantmentSeed = DataSlot.standalone();
        random.setSeed(enchantmentSeed.get());
        enchantmentSeed.set(player.getEnchantmentSeed());
        net.minecraft.world.item.ItemStack itemStack = CraftItemStack.asNMSCopy(itemToEnchant);
        int[] costs = new int[3];
        int[] enchantClue = new int[]{-1, -1, -1};
        int[] levelClue = new int[]{-1, -1, -1};
        int j;
        for (j = 0; j < 3; ++j) {
            costs[j] = EnchantmentHelper.getEnchantmentCost(random, j, shelves, itemStack);
            enchantClue[j] = -1;
            levelClue[j] = -1;
            if (costs[j] < j + 1) {
                costs[j] = 0;
            }
        }
        for (j = 0; j < 3; ++j) {
            if (costs[j] > 0) {
                random.setSeed(enchantmentSeed.get() + j);
                List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(random, itemStack, costs[j], false);
                if (itemStack.is(Items.BOOK) && list.size() > 1) {
                    list.remove(random.nextInt(list.size()));
                }
                if (list != null && !list.isEmpty()) {
                    EnchantmentInstance weightedRandomEnchant = list.get(random.nextInt(list.size()));
                    enchantClue[j] = BuiltInRegistries.ENCHANTMENT.getId(weightedRandomEnchant.enchantment);
                    levelClue[j] = weightedRandomEnchant.level;
                }
            }
        }
        for (j = 0; j < 3; ++j) {
            org.bukkit.enchantments.Enchantment enchantment = (enchantClue[j] >= 0) ? org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.ENCHANTMENT.getKey(BuiltInRegistries.ENCHANTMENT.byId(enchantClue[j])))) : null;
            offers[j] = (enchantment != null) ? new EnchantmentOffer(enchantment, levelClue[j], costs[j]) : null;
        }
        return offers;
    }

    @Override
    public HighlightBlocks highlightBlocks(Player player, NamedTextColor color, Location... locations) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ArrayList<Packet<ClientGamePacketListener>> packets = new ArrayList<>();
        ArrayList<String> entityUUIDs = new ArrayList<>();
        int[] entityIDs = new int[locations.length];
        int index = 0;
        for (Location location : locations) {
            UUID uuid = UUID.randomUUID();
            int entityID = SelfIncreaseEntityID.getAndIncrease();
            ClientboundAddEntityPacket entityPacket = new ClientboundAddEntityPacket(
                    entityID,
                    uuid,
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    0,
                    0,
                    EntityType.SLIME,
                    0,
                    Vec3.ZERO,
                    0
            );
            ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(
                    entityID,
                    List.of(
                            SynchedEntityData.DataValue.create(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), (byte) (0x20 | 0x40)),
                            SynchedEntityData.DataValue.create(new EntityDataAccessor<>(16, EntityDataSerializers.INT), 2)
                    )
            );
            entityUUIDs.add(uuid.toString());
            entityIDs[index++] = entityID;
            packets.add(entityPacket);
            packets.add(dataPacket);
        }
        String teamName = "sparrow_highlight_" + SelfIncreaseInt.getAndIncrease();
        PlayerTeam team = new PlayerTeam(MinecraftServer.getServer().getScoreboard(), teamName);
        team.setColor(ChatFormatting.valueOf(color.getName().toUpperCase(Locale.ENGLISH)));
        team.getPlayers().addAll(entityUUIDs);
        team.setCollisionRule(Team.CollisionRule.NEVER);
        ClientboundSetPlayerTeamPacket teamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        packets.add(teamPacket);
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
        serverPlayer.connection.send(bundlePacket);
        return new HighlightBlocks(entityIDs, teamName);
    }

    @Override
    public void removeClientSideTeam(Player player, String teamName) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundSetPlayerTeamPacket teamPacket = ClientboundSetPlayerTeamPacket.createRemovePacket(new PlayerTeam(MinecraftServer.getServer().getScoreboard(), teamName));
        serverPlayer.connection.send(teamPacket);
    }

    @Override
    public void addClientSideTeam(Player player, String teamName, List<String> members, String display, String prefix, String suffix, TeamVisibility tagVisibility, TeamVisibility messageVisibility, TeamCollisionRule collisionRule, TeamColor color, boolean allowFriendlyFire, boolean seeFriendlyInvisibles) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        PlayerTeam team = new PlayerTeam(MinecraftServer.getServer().getScoreboard(), teamName);
        team.setColor(ChatFormatting.valueOf(color.name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collisionRule.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(tagVisibility.name()));
        team.setDisplayName(CraftChatMessage.fromJSON(display));
        team.setPlayerPrefix(CraftChatMessage.fromJSON(prefix));
        team.setPlayerSuffix(CraftChatMessage.fromJSON(suffix));
        team.getPlayers().addAll(members);
        team.setAllowFriendlyFire(allowFriendlyFire);
        team.setSeeFriendlyInvisibles(seeFriendlyInvisibles);
        team.setDeathMessageVisibility(Team.Visibility.valueOf(messageVisibility.name()));
        ClientboundSetPlayerTeamPacket teamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        serverPlayer.connection.send(teamPacket);
    }

    @Override
    public void updateClientSideTeam(Player player, String teamName, String display, String prefix, String suffix, TeamVisibility tagVisibility, TeamVisibility messageVisibility, TeamCollisionRule collisionRule, TeamColor color, boolean allowFriendlyFire, boolean seeFriendlyInvisibles) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        PlayerTeam team = new PlayerTeam(MinecraftServer.getServer().getScoreboard(), teamName);
        team.setColor(ChatFormatting.valueOf(color.name()));
        team.setCollisionRule(Team.CollisionRule.valueOf(collisionRule.name()));
        team.setNameTagVisibility(Team.Visibility.valueOf(tagVisibility.name()));
        team.setDisplayName(CraftChatMessage.fromJSON(display));
        team.setPlayerPrefix(CraftChatMessage.fromJSON(prefix));
        team.setPlayerSuffix(CraftChatMessage.fromJSON(suffix));
        team.setAllowFriendlyFire(allowFriendlyFire);
        team.setSeeFriendlyInvisibles(seeFriendlyInvisibles);
        team.setDeathMessageVisibility(Team.Visibility.valueOf(messageVisibility.name()));
        ClientboundSetPlayerTeamPacket teamPacket = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, false);
        serverPlayer.connection.send(teamPacket);
    }

    @Override
    public void removeClientSideEntity(Player player, int... entityIDs) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entityIDs);
        serverPlayer.connection.send(packet);
    }

    @Override
    public void sendClientSideTeleportEntity(Player player, Location location, boolean onGround, int... entityIDs) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ArrayList<Packet<ClientGamePacketListener>> packets = new ArrayList<>();
        float ROTATION_FACTOR = 256.0F / 360.0F;
        float yaw = location.getYaw() * ROTATION_FACTOR;
        float pitch = location.getPitch() * ROTATION_FACTOR;
        for (int entityID : entityIDs) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeVarInt(entityID);
            buf.writeDouble(location.getX());
            buf.writeDouble(location.getY());
            buf.writeDouble(location.getZ());
            buf.writeByte((byte) yaw);
            buf.writeByte((byte) pitch);
            buf.writeBoolean(onGround);
            ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(buf);
            packets.add(packet);
        }
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
        serverPlayer.connection.send(bundlePacket);
    }

    @Override
    public void sendDebugMarker(Player player, Location location, String message, int duration, int color) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(Unpooled.buffer());
        friendlyByteBuf.writeBlockPos(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        friendlyByteBuf.writeInt(color);
        friendlyByteBuf.writeUtf(message);
        friendlyByteBuf.writeInt(duration);
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundCustomPayloadPacket(ClientboundCustomPayloadPacket.DEBUG_GAME_TEST_ADD_MARKER, friendlyByteBuf));
    }

    @Override
    public String getBiomeResourceLocation(Location location) {
        Biome biome = ((CraftWorld) location.getWorld()).getHandle().getNoiseBiome(location.getBlockX() >> 2, location.getBlockY() >> 2, location.getBlockZ() >> 2).value();
        ResourceLocation resourceLocation = biomeRegistry.getKey(biome);
        if (resourceLocation == null) {
            return "void";
        }
        return resourceLocation.toString();
    }

    @Override
    public FakeArmorStand createFakeArmorStand(Location location) {
        return new SparrowArmorStand(location);
    }

    @Override
    public FakeItemDisplay createFakeItemDisplay(Location location) {
        return new SparrowItemDisplay(location);
    }

    @Override
    public FakeTextDisplay createFakeTextDisplay(Location location) {
        return new SparrowTextDisplay(location);
    }

    @Override
    public void createBossBar(Player player, UUID uuid, String displayName, BossBarColor color, BossBarOverlay overlay, float progress, boolean createWorldFog, boolean playBossMusic, boolean darkenScreen) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(uuid);
        buf.writeEnum(addBossBarOperation);
        buf.writeComponent(CraftChatMessage.fromJSON(displayName));
        buf.writeFloat(progress);
        buf.writeEnum(BossEvent.BossBarColor.valueOf(color.name()));
        buf.writeEnum(BossEvent.BossBarOverlay.valueOf(overlay.name()));
        buf.writeByte(BossBarUtils.encodeProperties(darkenScreen, playBossMusic, createWorldFog));
        ClientboundBossEventPacket packet = new ClientboundBossEventPacket(buf);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void removeBossBar(Player player, UUID uuid) {
        ((CraftPlayer) player).getHandle().connection.send(ClientboundBossEventPacket.createRemovePacket(uuid));
    }

    @Override
    public void updateBossBarName(Player player, UUID uuid, String displayName) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(uuid);
        buf.writeEnum(updateBossBarNameOperation);
        buf.writeComponent(CraftChatMessage.fromJSON(displayName));
        ClientboundBossEventPacket packet = new ClientboundBossEventPacket(buf);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public void updateBossBarProgress(Player player, UUID uuid, float progress) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUUID(uuid);
        buf.writeEnum(updateBossBarProgressOperation);
        buf.writeFloat(progress);
        ClientboundBossEventPacket packet = new ClientboundBossEventPacket(buf);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    public boolean isFishingHookBit(FishHook hook) {
        FishingHook fishingHook = ((CraftFishHook) hook).getHandle();
        return fishingHook.getEntityData().get(dataBiting);
    }

    @Override
    public List<ItemStack> getFishingLoot(Player player, FishHook hook, ItemStack rod) {
        Location location = hook.getLocation();
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        LootParams lootparams = (new LootParams.Builder(level))
                .withParameter(LootContextParams.ORIGIN, CraftLocation.toVec3D(location))
                .withParameter(LootContextParams.TOOL, CraftItemStack.asNMSCopy(rod))
                .withParameter(LootContextParams.THIS_ENTITY, ((CraftFishHook) hook).getHandle())
                .withLuck((float) (rod.getEnchantmentLevel(Enchantment.LUCK) + Optional.ofNullable(player.getAttribute(Attribute.GENERIC_LUCK)).map(AttributeInstance::getValue).orElse(0d)))
                .create(LootContextParamSets.FISHING);
        LootTable loottable = level.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING);
        List<net.minecraft.world.item.ItemStack> list = loottable.getRandomItems(lootparams);
        return list.stream().filter(itemStack -> itemStack != null && !itemStack.isEmpty()).map(net.minecraft.world.item.ItemStack::getBukkitStack).toList();
    }

    @Override
    public void useItem(Player player, HandSlot handSlot, @Nullable ItemStack itemStack) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.gameMode.useItem(serverPlayer, ((CraftWorld) player.getWorld()).getHandle(), Optional.ofNullable(itemStack).map(stack -> ((CraftItemStack) itemStack).handle).orElse(serverPlayer.getItemBySlot(EquipmentSlot.valueOf(handSlot.name() + "HAND"))), InteractionHand.valueOf(handSlot.name() + "_HAND"));
    }

    @Override
    public Map<String, Integer> itemEnchantmentsToMap(Object item) {
        return Map.of();
    }

    @Override
    public int dropFakeItem(Player player, ItemStack itemStack, Location location) {
        UUID uuid = UUID.randomUUID();
        int entityID = SelfIncreaseEntityID.getAndIncrease();
        ClientboundAddEntityPacket entityPacket = new ClientboundAddEntityPacket(
                entityID, uuid,
                location.getX(), location.getY(), location.getZ(), 0, 0,
                EntityType.ITEM,
                0, Vec3.ZERO, 0
        );
        ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(
                entityID,
                List.of(
                        SynchedEntityData.DataValue.create(new EntityDataAccessor<>(8, EntityDataSerializers.ITEM_STACK), CraftItemStack.asNMSCopy(itemStack)),
                        SynchedEntityData.DataValue.create(new EntityDataAccessor<>(5, EntityDataSerializers.BOOLEAN), true)
                )
        );
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(List.of(entityPacket, dataPacket));
        ((CraftPlayer)player).getHandle().connection.send(bundlePacket);
        return entityID;
    }

    @Override
    public void sendClientSideEntityMotion(Player player, Vector vector, int... entityIDs) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ArrayList<Packet<ClientGamePacketListener>> packets = new ArrayList<>();
        Vec3 vec3 = CraftVector.toNMS(vector);
        for (int entityID : entityIDs) {
            ClientboundSetEntityMotionPacket packet = new ClientboundSetEntityMotionPacket(entityID, vec3);
            packets.add(packet);
        }
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
        serverPlayer.connection.send(bundlePacket);
    }

    @Override
    public FluidData getFluidData(Location location) {
        World world = location.getWorld();
        FluidState state = ((CraftWorld) world).getHandle().getFluidState(CraftLocation.toBlockPosition(location));
        return SparrowFluidData.createData(state);
    }
}
