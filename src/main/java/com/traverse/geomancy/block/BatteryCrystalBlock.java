package com.traverse.geomancy.block;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.traverse.geomancy.block.entity.BatteryCrystalBlockEntity;
import com.traverse.geomancy.resonance.BatterySize;

public class BatteryCrystalBlock extends Block implements EntityBlock {
    // Discrete rather than continuous so an insert/extract only triggers a blockstate (and
    // relight) update when the visible brightness actually changes, not on every tick.
    public static final IntegerProperty FILL_LEVEL = IntegerProperty.create("fill_level", 0, 4);
    private static final int[] LIGHT_BY_LEVEL = {0, 4, 8, 12, 15};

    // The model's elements span the full y=0..16 range (touching the floor, peaking at the
    // block's own ceiling), so one shape now safely serves both outline and collision -
    // slightly padded horizontally past the model's 4..14 / 3..13 footprint to cover the
    // two shards with compound rotations.
    private static final VoxelShape SHAPE = Shapes.box(0.1875, 0.0, 0.1875, 0.8125, 1.0, 0.8125);

    private final BatterySize size;

    public BatteryCrystalBlock(BatterySize size, BlockBehaviour.Properties properties) {
        super(properties);
        this.size = size;
        registerDefaultState(stateDefinition.any().setValue(FILL_LEVEL, 0));
    }

    public BatterySize size() {
        return size;
    }

    public static int lightForLevel(int fillLevel) {
        return LIGHT_BY_LEVEL[fillLevel];
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILL_LEVEL);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatteryCrystalBlockEntity(pos, state);
    }
}
