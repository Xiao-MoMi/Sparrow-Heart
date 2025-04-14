package net.momirealms.sparrow.heart.impl.reobf_1_21_r4;

import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.momirealms.sparrow.heart.feature.fluid.FallingFluidData;

public class SparrowFallingFluidData extends SparrowFluidData implements FallingFluidData {

    public SparrowFallingFluidData(final FluidState state) {
        super(state);
    }

    @Override
    public boolean isFalling() {
        return this.getState().getValue(FlowingFluid.FALLING);
    }
}
