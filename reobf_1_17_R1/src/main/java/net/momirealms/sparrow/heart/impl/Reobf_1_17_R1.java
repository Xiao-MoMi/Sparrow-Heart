package net.momirealms.sparrow.heart.impl;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.argument.HandSlot;
import net.momirealms.sparrow.heart.argument.NamedTextColor;
import net.momirealms.sparrow.heart.feature.highlight.HighlightBlocks;
import net.momirealms.sparrow.heart.util.SelfIncreaseInt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Reobf_1_17_R1 extends SparrowHeart {

    private final DedicatedServer dedicatedServer = ((CraftServer) Bukkit.getServer()).getServer();

    private final Registry<Biome> biomeRegistry = dedicatedServer.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);

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
            packetListeners.add(new ClientboundSetTitleTextPacket(new TextComponent("")));
        }
        if (subTitleJson != null) {
            packetListeners.add(new ClientboundSetSubtitleTextPacket(Objects.requireNonNull(Component.Serializer.fromJson(subTitleJson))));
        }
        for (Packet<ClientGamePacketListener> packet: packetListeners) {
            serverPlayer.connection.send(packet);
        }
    }

    @Override
    public void sendToast(Player player, ItemStack icon, String titleJson, String advancementType) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(icon);
        DisplayInfo displayInfo = new DisplayInfo(nmsStack, Objects.requireNonNull(Component.Serializer.fromJson(titleJson)), Component.nullToEmpty(""), null, FrameType.valueOf(advancementType), true, false, true);
        AdvancementRewards advancementRewards = AdvancementRewards.EMPTY;
        ResourceLocation id = new ResourceLocation("sparrow", "toast");
        Criterion criterion = new Criterion(new ImpossibleTrigger.TriggerInstance());
        HashMap<String, Criterion> criteria = new HashMap<>(Map.of("impossible", criterion));
        String[][] requirements = {{"impossible"}};
        Advancement advancement = new Advancement(id, null, displayInfo, advancementRewards, criteria, requirements);
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
        ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(player.getEntityId(), List.of(Pair.of(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(totem))));
        var connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(equipmentPacket);
        ClientboundEntityEventPacket entityDataPacket = new ClientboundEntityEventPacket(((CraftPlayer) player).getHandle(), (byte) 35);
        connection.send(entityDataPacket);
        equipmentPacket = new ClientboundSetEquipmentPacket(player.getEntityId(), List.of(Pair.of(EquipmentSlot.OFFHAND, CraftItemStack.asNMSCopy(previousItem))));
        connection.send(equipmentPacket);
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
        Random random = new Random();
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
                    enchantClue[j] = Registry.ENCHANTMENT.getId(weightedRandomEnchant.enchantment);
                    levelClue[j] = weightedRandomEnchant.level;
                }
            }
        }
        for (j = 0; j < 3; ++j) {
            org.bukkit.enchantments.Enchantment enchantment = (enchantClue[j] >= 0) ? org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(Registry.ENCHANTMENT.getKey(Registry.ENCHANTMENT.byId(enchantClue[j])))) : null;
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
            Slime slime = new Slime(EntityType.SLIME, serverPlayer.getLevel());
            ClientboundAddEntityPacket entityPacket = new ClientboundAddEntityPacket(
                    slime.getId(),
                    slime.getUUID(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    0,
                    0,
                    EntityType.SLIME,
                    0,
                    Vec3.ZERO
            );
            SynchedEntityData entityData = new SynchedEntityData(slime);
            entityData.set(new EntityDataAccessor<>(0, EntityDataSerializers.BYTE), (byte) (0x20 | 0x40));
            entityData.set(new EntityDataAccessor<>(16, EntityDataSerializers.INT), 2);
            ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(
                    slime.getId(),
                    entityData,
                    false
            );
            entityUUIDs.add(slime.getUUID().toString());
            entityIDs[index++] = slime.getId();
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
        for (Packet<ClientGamePacketListener> packet : packets) {
            serverPlayer.connection.send(packet);
        }
        return new HighlightBlocks(entityIDs, teamName);
    }

    @Override
    public void removeClientSideTeam(Player player, String teamName) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundSetPlayerTeamPacket teamPacket = ClientboundSetPlayerTeamPacket.createRemovePacket(new PlayerTeam(MinecraftServer.getServer().getScoreboard(), teamName));
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
        float ROTATION_FACTOR = 256.0F / 360.0F;
        float yaw = location.getYaw() * ROTATION_FACTOR;
        float pitch = location.getPitch() * ROTATION_FACTOR;
        for (int entityID : entityIDs) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeInt(entityID);
            buf.writeDouble(location.getX());
            buf.writeDouble(location.getY());
            buf.writeDouble(location.getZ());
            buf.writeByte((byte) yaw);
            buf.writeByte((byte) pitch);
            buf.writeBoolean(onGround);
            ClientboundTeleportEntityPacket packet = new ClientboundTeleportEntityPacket(buf);
            serverPlayer.connection.send(packet);
        }
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
        Biome biome = ((CraftWorld) location.getWorld()).getHandle().getNoiseBiome(location.getBlockX() >> 2, location.getBlockY() >> 2, location.getBlockZ() >> 2);
        ResourceLocation resourceLocation = biomeRegistry.getKey(biome);
        if (resourceLocation == null) {
            return "void";
        }
        return resourceLocation.toString();
    }
}
