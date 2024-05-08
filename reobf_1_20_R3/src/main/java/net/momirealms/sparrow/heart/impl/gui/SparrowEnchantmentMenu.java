package net.momirealms.sparrow.heart.impl.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventoryEnchanting;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class SparrowEnchantmentMenu extends EnchantmentMenu {

    static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = new ResourceLocation("item/empty_slot_lapis_lazuli");
    private final Container enchantSlots;
    private final ContainerLevelAccess access;
    private final RandomSource random;
    private final DataSlot enchantmentSeed;
    public final int[] costs;
    public final int[] enchantClue;
    public final int[] levelClue;
    // CraftBukkit start
    private CraftInventoryView bukkitEntity = null;
    private final Player player;
    // CraftBukkit end
    // Sparrow start
    private final int shelves;
    // Sparrow end

    public static void registerSparrowEnchantmentMenu() {
        var registryWritable = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.MENU);
        try {
            Class<?> registryMaterialsClass = Class.forName("net.minecraft.core.RegistryMaterials");
            Field frozen = registryMaterialsClass.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registryWritable, false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Method registerMethod = MenuType.class.getDeclaredMethod("a", String.class, Class.forName("net.minecraft.world.inventory.Containers$Supplier"));
            registerMethod.setAccessible(true);
            Class<?> menuSupplierClass = Class.forName("net.minecraft.world.inventory.Containers$Supplier");
            Object supplier = java.lang.reflect.Proxy.newProxyInstance(
                    menuSupplierClass.getClassLoader(),
                    new Class<?>[]{menuSupplierClass},
                    (proxy, method, args) -> {
                        if (method.getName().equals("create")) {
                            int syncId = (int) args[0];
                            Inventory playerInventory = (Inventory) args[1];
                            return new SparrowEnchantmentMenu(syncId, playerInventory);
                        }
                        return null;
                    }
            );

            Field enchantmentField = MenuType.class.getDeclaredField("n");
            enchantmentField.setAccessible(true);
            VarHandle MODIFIERS;

            var lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
            int mods = enchantmentField.getModifiers();

            if (Modifier.isFinal(mods)) {
                MODIFIERS.set(enchantmentField, mods & ~Modifier.FINAL);
            }

            enchantmentField.set(null, registerMethod.invoke(null, "enchantment", supplier));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            Class<?> registryMaterialsClass = Class.forName("net.minecraft.core.RegistryMaterials");
            Field frozen = registryMaterialsClass.getDeclaredField("l");
            frozen.setAccessible(true);
            frozen.set(registryWritable, true);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException |
                 ClassNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    public SparrowEnchantmentMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL, 0);
    }

    public SparrowEnchantmentMenu(int syncId, Inventory playerInventory, int shelves) {
        this(syncId, playerInventory, ContainerLevelAccess.NULL, shelves);
    }

    public SparrowEnchantmentMenu(int syncId, Inventory playerInventory, ContainerLevelAccess context, int shelves) {
        super(syncId, playerInventory, context);
        this.shelves = shelves;
        this.enchantSlots = new SimpleContainer(this.createBlockHolder(context), 2) {
            @Override
            public void setChanged() {
                super.setChanged();
                SparrowEnchantmentMenu.this.slotsChanged(this);
            }

            @Override
            public @NotNull Location getLocation() {
                return context.getLocation();
            }
        };
        this.random = RandomSource.create();
        this.enchantmentSeed = DataSlot.standalone();
        this.costs = new int[3];
        this.enchantClue = new int[]{-1, -1, -1};
        this.levelClue = new int[]{-1, -1, -1};
        this.access = context;
        this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, SparrowEnchantmentMenu.EMPTY_SLOT_LAPIS_LAZULI);
            }
        });

        int j;

        for (j = 0; j < 3; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 84 + j * 18));
            }
        }

        for (j = 0; j < 9; ++j) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }

        this.addDataSlot(DataSlot.shared(this.costs, 0));
        this.addDataSlot(DataSlot.shared(this.costs, 1));
        this.addDataSlot(DataSlot.shared(this.costs, 2));
        this.addDataSlot(this.enchantmentSeed).set(playerInventory.player.getEnchantmentSeed());
        this.addDataSlot(DataSlot.shared(this.enchantClue, 0));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 1));
        this.addDataSlot(DataSlot.shared(this.enchantClue, 2));
        this.addDataSlot(DataSlot.shared(this.levelClue, 0));
        this.addDataSlot(DataSlot.shared(this.levelClue, 1));
        this.addDataSlot(DataSlot.shared(this.levelClue, 2));
        // CraftBukkit start
        this.player = (Player) playerInventory.player.getBukkitEntity();
        // CraftBukkit end
    }

    @Override
    public void slotsChanged(Container inventory) {
        if (inventory == this.enchantSlots) {
            ItemStack itemstack = inventory.getItem(0);

            if (!itemstack.isEmpty()) {
                this.access.execute((world, blockposition) -> {
                    int i = shelves;

                    this.random.setSeed(this.enchantmentSeed.get());

                    int j;

                    for (j = 0; j < 3; ++j) {
                        this.costs[j] = EnchantmentHelper.getEnchantmentCost(this.random, j, i, itemstack);
                        this.enchantClue[j] = -1;
                        this.levelClue[j] = -1;
                        if (this.costs[j] < j + 1) {
                            this.costs[j] = 0;
                        }
                    }

                    for (j = 0; j < 3; ++j) {
                        if (this.costs[j] > 0) {
                            List<EnchantmentInstance> list = this.getEnchantmentList(itemstack, j, this.costs[j]);

                            if (list != null && !list.isEmpty()) {
                                EnchantmentInstance weightedrandomenchant = (EnchantmentInstance) list.get(this.random.nextInt(list.size()));

                                this.enchantClue[j] = BuiltInRegistries.ENCHANTMENT.getId(weightedrandomenchant.enchantment);
                                this.levelClue[j] = weightedrandomenchant.level;
                            }
                        }
                    }

                    // CraftBukkit start
                    CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
                    org.bukkit.enchantments.EnchantmentOffer[] offers = new EnchantmentOffer[3];
                    for (j = 0; j < 3; ++j) {
                        org.bukkit.enchantments.Enchantment enchantment = (this.enchantClue[j] >= 0) ? org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.ENCHANTMENT.getKey(BuiltInRegistries.ENCHANTMENT.byId(this.enchantClue[j])))) : null;
                        offers[j] = (enchantment != null) ? new EnchantmentOffer(enchantment, this.levelClue[j], this.costs[j]) : null;
                    }

                    PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(this.player, this.getBukkitView(), this.access.getLocation().getBlock(), item, offers, i);
                    event.setCancelled(!itemstack.isEnchantable());
                    world.getCraftServer().getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        for (j = 0; j < 3; ++j) {
                            this.costs[j] = 0;
                            this.enchantClue[j] = -1;
                            this.levelClue[j] = -1;
                        }
                        return;
                    }

                    for (j = 0; j < 3; j++) {
                        EnchantmentOffer offer = event.getOffers()[j];
                        if (offer != null) {
                            this.costs[j] = offer.getCost();
                            this.enchantClue[j] = BuiltInRegistries.ENCHANTMENT.getId(BuiltInRegistries.ENCHANTMENT.get(CraftNamespacedKey.toMinecraft(offer.getEnchantment().getKey())));
                            this.levelClue[j] = offer.getEnchantmentLevel();
                        } else {
                            this.costs[j] = 0;
                            this.enchantClue[j] = -1;
                            this.levelClue[j] = -1;
                        }
                    }
                    // CraftBukkit end

                    this.broadcastChanges();
                });
            } else {
                for (int i = 0; i < 3; ++i) {
                    this.costs[i] = 0;
                    this.enchantClue[i] = -1;
                    this.levelClue[i] = -1;
                }
            }
        }

    }

    @Override
    public boolean clickMenuButton(net.minecraft.world.entity.player.Player player, int id) {
        if (id >= 0 && id < this.costs.length) {
            ItemStack itemstack = this.enchantSlots.getItem(0);
            ItemStack itemstack1 = this.enchantSlots.getItem(1);
            int j = id + 1;

            if ((itemstack1.isEmpty() || itemstack1.getCount() < j) && !player.getAbilities().instabuild) {
                return false;
            } else if (this.costs[id] > 0 && !itemstack.isEmpty() && (player.experienceLevel >= j && player.experienceLevel >= this.costs[id] || player.getAbilities().instabuild)) {
                this.access.execute((world, blockposition) -> {
                    ItemStack itemstack2 = itemstack; // Paper - diff on change
                    List<EnchantmentInstance> list = this.getEnchantmentList(itemstack, id, this.costs[id]);

                    // CraftBukkit start
                    if (true || !list.isEmpty()) {
                        // entityhuman.onEnchantmentPerformed(itemstack, j); // Moved down
                        Map<Enchantment, Integer> enchants = new java.util.HashMap<org.bukkit.enchantments.Enchantment, Integer>();
                        for (EnchantmentInstance instance : list) {
                            enchants.put(org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.ENCHANTMENT.getKey(instance.enchantment))), instance.level);
                        }
                        CraftItemStack item = CraftItemStack.asCraftMirror(itemstack2);

                        org.bukkit.enchantments.Enchantment hintedEnchantment = org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.ENCHANTMENT.getKey(net.minecraft.world.item.enchantment.Enchantment.byId(this.enchantClue[id]))));
                        int hintedEnchantmentLevel = this.levelClue[id];
                        EnchantItemEvent event = new EnchantItemEvent((Player) player.getBukkitEntity(), this.getBukkitView(), this.access.getLocation().getBlock(), item, this.costs[id], enchants, hintedEnchantment, hintedEnchantmentLevel, id);
                        world.getCraftServer().getPluginManager().callEvent(event);

                        int level = event.getExpLevelCost();
                        if (event.isCancelled() || (level > player.experienceLevel && !player.getAbilities().instabuild) || event.getEnchantsToAdd().isEmpty()) {
                            return;
                        }
                        // CraftBukkit end
                        // Paper start
                        itemstack2 = org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack.getOrCloneOnMutation(item, event.getItem());
                        if (itemstack2 != itemstack) {
                            this.enchantSlots.setItem(0, itemstack2);
                        }
                        boolean flag = itemstack2.is(Items.BOOK);
                        // Paper end

                        if (flag) {
                            CompoundTag nbttagcompound = itemstack2.getTag(); // Paper - move up
                            itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                            // Paper - move up

                            if (nbttagcompound != null) {
                                itemstack2.setTag(nbttagcompound.copy());
                            }

                            this.enchantSlots.setItem(0, itemstack2);
                        }

                        // CraftBukkit start
                        for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
                            try {
                                if (flag) {
                                    NamespacedKey enchantId = entry.getKey().getKey();
                                    net.minecraft.world.item.enchantment.Enchantment nms = BuiltInRegistries.ENCHANTMENT.get(CraftNamespacedKey.toMinecraft(enchantId));
                                    if (nms == null) {
                                        continue;
                                    }

                                    EnchantmentInstance weightedrandomenchant = new EnchantmentInstance(nms, entry.getValue());
                                    EnchantedBookItem.addEnchantment(itemstack2, weightedrandomenchant);
                                } else {
                                    CraftItemStack.asCraftMirror(itemstack2).addUnsafeEnchantment(entry.getKey(), entry.getValue()); // Paper
                                }
                            } catch (IllegalArgumentException e) {
                                /* Just swallow invalid enchantments */
                            }
                        }

                        player.onEnchantmentPerformed(itemstack, j);
                        // CraftBukkit end

                        // CraftBukkit - TODO: let plugins change this
                        if (!player.getAbilities().instabuild) {
                            itemstack1.shrink(j);
                            if (itemstack1.isEmpty()) {
                                this.enchantSlots.setItem(1, ItemStack.EMPTY);
                            }
                        }

                        player.awardStat(Stats.ENCHANT_ITEM);
                        if (player instanceof ServerPlayer) {
                            CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayer) player, itemstack2, j);
                        }

                        this.enchantSlots.setChanged();
                        this.enchantmentSeed.set(player.getEnchantmentSeed());
                        this.slotsChanged(this.enchantSlots);
                        world.playSound(null, blockposition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
                    }

                });
                return true;
            } else {
                return false;
            }
        } else {
            Component ichatbasecomponent = player.getName();

            Util.logAndPauseIfInIde(ichatbasecomponent + " pressed invalid button id: " + id);
            return false;
        }
    }

    private List<EnchantmentInstance> getEnchantmentList(ItemStack stack, int slot, int level) {
        this.random.setSeed(this.enchantmentSeed.get() + slot);
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(this.random, stack, level, false);

        if (stack.is(Items.BOOK) && list.size() > 1) {
            list.remove(this.random.nextInt(list.size()));
        }

        return list;
    }

    public int getGoldCount() {
        ItemStack itemstack = this.enchantSlots.getItem(1);

        return itemstack.isEmpty() ? 0 : itemstack.getCount();
    }

    public int getEnchantmentSeed() {
        return this.enchantmentSeed.get();
    }

    @Override
    public void removed(net.minecraft.world.entity.player.Player player) {
        super.removed(player);
        this.access.execute((world, blockposition) -> {
            this.clearContainer(player, this.enchantSlots);
        });
    }

    @Override
    public boolean stillValid(net.minecraft.world.entity.player.Player player) {
        if (!this.checkReachable) return true; // CraftBukkit
        return stillValid(this.access, player, Blocks.ENCHANTING_TABLE);
    }

    @Override
    public ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int slot) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot1 = (Slot) this.slots.get(slot);

        if (slot1 != null && slot1.hasItem()) {
            ItemStack itemstack1 = slot1.getItem();

            itemstack = itemstack1.copy();
            if (slot == 0) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (slot == 1) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (itemstack1.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(itemstack1, 1, 2, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (((Slot) this.slots.get(0)).hasItem() || !((Slot) this.slots.get(0)).mayPlace(itemstack1)) {
                    return ItemStack.EMPTY;
                }

                ItemStack itemstack2 = itemstack1.copyWithCount(1);

                itemstack1.shrink(1);
                ((Slot) this.slots.get(0)).setByPlayer(itemstack2);
            }

            if (itemstack1.isEmpty()) {
                slot1.setByPlayer(ItemStack.EMPTY);
            } else {
                slot1.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot1.onTake(player, itemstack1);
        }

        return itemstack;
    }

    // CraftBukkit start
    @Override
    public CraftInventoryView getBukkitView() {
        if (this.bukkitEntity != null) {
            return this.bukkitEntity;
        }

        CraftInventoryEnchanting inventory = new CraftInventoryEnchanting(this.enchantSlots);
        this.bukkitEntity = new CraftInventoryView(this.player, inventory, this);
        return this.bukkitEntity;
    }
    // CraftBukkit end
}
