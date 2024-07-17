package net.momirealms.sparrow.heart.feature.fluid;

import org.bukkit.Fluid;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface FluidData {

    @NotNull
    Fluid getFluidType();

    @NotNull
    Vector computeFlowDirection(@NotNull Location location);

    @Range(
            from = 0L,
            to = 8L
    ) int getLevel();

    @Range(
            from = 0L,
            to = 1L
    ) float computeHeight(@NotNull Location location);

    boolean isSource();
}
