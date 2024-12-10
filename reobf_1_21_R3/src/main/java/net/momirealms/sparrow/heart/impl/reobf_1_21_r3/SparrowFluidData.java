package net.momirealms.sparrow.heart.impl.reobf_1_21_r3;

import com.google.common.base.Preconditions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.momirealms.sparrow.heart.feature.fluid.FluidData;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftFluid;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SparrowFluidData implements FluidData {

    private static final Map<Class<? extends Fluid>, Function<FluidState, FluidData>> MAP = new HashMap<>();

    static void register(final Class<? extends net.minecraft.world.level.material.Fluid> fluid, final Function<FluidState, FluidData> creator) {
        Preconditions.checkState(MAP.put(fluid, creator) == null, "Duplicate mapping %s->%s", fluid, creator);
        MAP.put(fluid, creator);
    }

    public static FluidData createData(final FluidState state) {
        return MAP.getOrDefault(state.getType().getClass(), SparrowFluidData::new).apply(state);
    }

    private final FluidState state;

    protected SparrowFluidData(final FluidState state) {
        this.state = state;
    }

    public FluidState getState() {
        return this.state;
    }

    @Override
    public final @NotNull org.bukkit.Fluid getFluidType() {
        return CraftFluid.minecraftToBukkit(this.state.getType());
    }

    @Override
    public @NotNull Vector computeFlowDirection(final Location location) {
        Preconditions.checkArgument(location.getWorld() != null, "Cannot compute flow direction on world-less location");
        return CraftVector.toBukkit(this.state.getFlow(
                ((CraftWorld) location.getWorld()).getHandle(),
                CraftLocation.toBlockPosition(location)
        ));
    }

    @Override
    public int getLevel() {
        return this.state.getAmount();
    }

    @Override
    public float computeHeight(@NotNull final Location location) {
        Preconditions.checkArgument(location.getWorld() != null, "Cannot compute height on world-less location");
        return this.state.getHeight(((CraftWorld) location.getWorld()).getHandle(), CraftLocation.toBlockPosition(location));
    }

    @Override
    public boolean isSource() {
        return this.state.isSource();
    }

    @Override
    public int hashCode() {
        return this.state.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof final SparrowFluidData fluidData && this.state.equals(fluidData.state);
    }

    @Override
    public String toString() {
        return "SparrowFluidData{" + this.state + "}";
    }
}
