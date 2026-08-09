package com.traverse.geomancy.block.entity;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.block.ResonantBrazierBlock;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.resonance.ResonanceStorage;

public class ResonantBrazierBlockEntity extends BlockEntity {
    public static final int FUEL_SLOT = 0;
    public static final int CRYSTAL_SLOT = 1;
    private static final int RESONANCE_PER_PULSE = 5;
    private static final int PULSE_INTERVAL = 20;
    private static final int BREAK_CHECK_INTERVAL = 100;
    private static final float BREAK_CHANCE = 0.1F;

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int burnTimeRemaining;
    private int resonanceSinceBreakCheck;

    public ResonantBrazierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_BRAZIER.get(), pos, state);
    }

    public boolean insert(ItemStack held) {
        if (held.is(Items.AMETHYST_SHARD) && items.get(CRYSTAL_SLOT).isEmpty()) {
            items.set(CRYSTAL_SLOT, held.split(1));
            sync();
            return true;
        }
        if (!isFuel(held)) {
            return false;
        }
        ItemStack fuel = items.get(FUEL_SLOT);
        if (fuel.isEmpty()) {
            items.set(FUEL_SLOT, held.split(1));
        } else if (ItemStack.isSameItemSameComponents(fuel, held) && fuel.getCount() < fuel.getMaxStackSize()) {
            fuel.grow(1);
            held.shrink(1);
        } else {
            return false;
        }
        sync();
        return true;
    }

    public ItemStack removeIngredient() {
        int slot = items.get(CRYSTAL_SLOT).isEmpty() ? FUEL_SLOT : CRYSTAL_SLOT;
        ItemStack removed = items.get(slot);
        if (removed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        items.set(slot, ItemStack.EMPTY);
        extinguish();
        sync();
        return removed;
    }

    public boolean ignite() {
        if (isLit() || items.get(CRYSTAL_SLOT).isEmpty()) {
            return false;
        }
        if (burnTimeRemaining <= 0 && !consumeFuel()) {
            return false;
        }
        setLit(true);
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.1F);
        }
        return true;
    }

    public List<ItemStack> items() {
        return items;
    }

    public int burnTimeRemaining() {
        return burnTimeRemaining;
    }

    public void dropContents(ServerLevel level) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.7,
                        worldPosition.getZ() + 0.5, stack.copy()));
            }
        }
        items.clear();
        extinguish();
        sync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonantBrazierBlockEntity brazier) {
        if (!brazier.isLit()) {
            return;
        }
        if (brazier.items.get(CRYSTAL_SLOT).isEmpty() || level.isRainingAt(pos.above())) {
            brazier.extinguish();
            return;
        }

        brazier.burnTimeRemaining--;
        if (level.getGameTime() % PULSE_INTERVAL == 0) {
            int generated = brazier.generate(level);
            brazier.resonanceSinceBreakCheck += generated;
            while (brazier.resonanceSinceBreakCheck >= BREAK_CHECK_INTERVAL) {
                brazier.resonanceSinceBreakCheck -= BREAK_CHECK_INTERVAL;
                if (level.getRandom().nextFloat() < BREAK_CHANCE) {
                    brazier.items.set(CRYSTAL_SLOT, ItemStack.EMPTY);
                    brazier.extinguish();
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.25F);
                    return;
                }
            }
        }

        if (brazier.burnTimeRemaining <= 0 && !brazier.consumeFuel()) {
            brazier.extinguish();
        } else {
            brazier.setChanged();
        }
    }

    private int generate(Level level) {
        int remaining = RESONANCE_PER_PULSE;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ResonanceStorage storage) {
                remaining -= storage.insertResonance(remaining, false);
                if (remaining == 0) {
                    break;
                }
            }
        }
        int generated = RESONANCE_PER_PULSE - remaining;
        if (generated > 0) {
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.25F, 1.6F);
        }
        return generated;
    }

    private boolean consumeFuel() {
        ItemStack fuel = items.get(FUEL_SLOT);
        if (fuel.isEmpty() || level == null) {
            return false;
        }
        int duration = fuel.getBurnTime(RecipeType.SMELTING, level.fuelValues());
        if (duration <= 0) {
            return false;
        }
        fuel.shrink(1);
        burnTimeRemaining = duration;
        sync();
        return true;
    }

    private static boolean isFuel(ItemStack stack) {
        return stack.is(ItemTags.COALS) || stack.is(Items.COAL_BLOCK);
    }

    private boolean isLit() {
        return getBlockState().getValue(ResonantBrazierBlock.LIT);
    }

    private void extinguish() {
        setLit(false);
    }

    private void setLit(boolean lit) {
        if (level != null && getBlockState().getValue(ResonantBrazierBlock.LIT) != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(ResonantBrazierBlock.LIT, lit), Block.UPDATE_ALL);
        }
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items, true);
        output.putInt("burn_time_remaining", burnTimeRemaining);
        output.putInt("resonance_since_break_check", resonanceSinceBreakCheck);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        burnTimeRemaining = input.getIntOr("burn_time_remaining", 0);
        resonanceSinceBreakCheck = input.getIntOr("resonance_since_break_check", 0);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }
}
