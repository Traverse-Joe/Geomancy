package com.traverse.geomancy.block.entity;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.traverse.geomancy.Config;
import com.traverse.geomancy.essence.EssenceCharge;
import com.traverse.geomancy.network.EssenceProvider;
import com.traverse.geomancy.network.EssenceRelay;
import com.traverse.geomancy.network.LinkResult;
import com.traverse.geomancy.network.RelayLink;
import com.traverse.geomancy.recipe.TransmutationInput;
import com.traverse.geomancy.recipe.TransmutationRecipe;
import com.traverse.geomancy.recipe.TransmutationResult;
import com.traverse.geomancy.recipe.TransmutationTarget;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModRecipes;

public class ItemPedestalBlockEntity extends BlockEntity implements EssenceRelay, Container {
    private static final int PROGRESS_SYNC_INTERVAL = 10;

    private final RelayLink link;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private EssenceCharge charge = EssenceCharge.EMPTY;
    private int progress;
    private int duration;
    private boolean completedOutput;

    public ItemPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_PEDESTAL.get(), pos, state);
        link = new RelayLink(pos);
    }

    public boolean startProcessing(Level level) {
        Optional<RecipeHolder<TransmutationRecipe>> recipe = findRecipe(level);
        if (recipe.isEmpty() || !(recipe.get().value().result() instanceof TransmutationResult.AsItem)) {
            return false;
        }
        int cost = recipe.get().value().cost();
        if (charge.amount() < cost) {
            return false;
        }
        progress = 1;
        duration = recipe.get().value().duration();
        setChangedAndSync();
        return true;
    }

    public boolean isProcessing() {
        return progress > 0;
    }

    public int progress() {
        return progress;
    }

    public int duration() {
        return duration;
    }

    public boolean hasCompletedOutput() {
        return completedOutput && !isEmpty();
    }

    private Optional<RecipeHolder<TransmutationRecipe>> findRecipe(Level level) {
        if (!(level instanceof ServerLevel serverLevel) || items.getFirst().isEmpty() || charge.isEmpty()) {
            return Optional.empty();
        }
        var input = new TransmutationInput(new TransmutationTarget.OfItem(items.getFirst()), charge.essence(), charge.form());
        return serverLevel.recipeAccess().getRecipeFor(ModRecipes.TRANSMUTATION_TYPE.get(), input, level);
    }

    private void pullEssence(Level level) {
        BlockPos upstream = link.upstream();
        if (!link.valid() || upstream == null || charge.amount() >= capacity()) {
            return;
        }
        if (!(level.getBlockEntity(upstream) instanceof EssenceProvider source)) {
            link.invalidate();
            setChangedAndSync();
            return;
        }
        EssenceCharge available = source.charge();
        if (available.isEmpty() || !charge.compatibleWith(available.essence(), available.form())) {
            return;
        }
        int wanted = Math.min(Config.RELAY_TRANSFER_AMOUNT.get(),
                Math.min(available.amount(), capacity() - charge.amount()));
        int taken = source.extract(wanted, false);
        if (taken > 0) {
            charge = charge.grow(available.essence(), available.form(), taken);
            setChangedAndSync();
        }
    }

    private void tickProcessing(Level level) {
        Optional<RecipeHolder<TransmutationRecipe>> holder = findRecipe(level);
        if (holder.isEmpty() || !(holder.get().value().result() instanceof TransmutationResult.AsItem(var template))) {
            clearProcessing();
            return;
        }
        TransmutationRecipe recipe = holder.get().value();
        duration = recipe.duration();
        if (charge.amount() < recipe.cost()) {
            return;
        }
        if (++progress < duration) {
            if (progress % PROGRESS_SYNC_INTERVAL == 0) {
                setChangedAndSync();
            } else {
                setChanged();
            }
            return;
        }
        ItemStack input = items.getFirst();
        input.shrink(1);
        ItemStack result = template.create();
        if (input.isEmpty()) {
            items.set(0, result);
        } else if (ItemStack.isSameItemSameComponents(input, result)
                && input.getCount() + result.getCount() <= input.getMaxStackSize()) {
            input.grow(result.getCount());
        } else {
            clearProcessing();
            return;
        }
        extract(recipe.cost(), false);
        completedOutput = true;
        level.playSound(null, worldPosition, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.7F, 1.2F);
        clearProcessing();
    }

    private void clearProcessing() {
        progress = 0;
        duration = 0;
        setChangedAndSync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ItemPedestalBlockEntity pedestal) {
        long time = level.getGameTime();
        if (pedestal.link.dueForRevalidate(time) && pedestal.link.revalidate(level)) {
            pedestal.setChangedAndSync();
        }
        if (pedestal.link.dueForTransfer(time)) {
            pedestal.pullEssence(level);
        }
        if (pedestal.isProcessing()) {
            pedestal.tickProcessing(level);
        } else if (!pedestal.isEmpty()) {
            pedestal.startProcessing(level);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ItemPedestalBlockEntity pedestal) {
        if (pedestal.progress > 0 && pedestal.progress < pedestal.duration) {
            pedestal.progress++;
        }
    }

    @Override
    public @Nullable BlockPos upstream() {
        return link.upstream();
    }

    @Override
    public LinkResult setUpstream(Level level, @Nullable BlockPos pos) {
        LinkResult result = link.set(level, pos);
        if (result == LinkResult.OK) {
            setChangedAndSync();
        }
        return result;
    }

    @Override
    public boolean linkValid() {
        return link.valid();
    }

    @Override
    public void markLinkDirty() {
        link.markDirty();
    }

    @Override
    public EssenceCharge charge() {
        return charge;
    }

    @Override
    public int capacity() {
        return Config.PEDESTAL_BUFFER_CAPACITY.get();
    }

    @Override
    public int extract(int amount, boolean simulate) {
        int taken = Math.min(amount, charge.amount());
        if (!simulate && taken > 0) {
            charge = charge.with(charge.amount() - taken);
            setChangedAndSync();
        }
        return taken;
    }

    public void setChangedAndSync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.getFirst().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            if (items.get(slot).isEmpty()) {
                completedOutput = false;
            }
            setChangedAndSync();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(items, slot);
        completedOutput = false;
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        completedOutput = false;
        clearProcessing();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
        clearProcessing();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            link.onLoad(level);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null) {
            link.onUnload(level);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            link.onUnload(level);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        link.save(output);
        ContainerHelper.saveAllItems(output, items, true);
        output.store("charge", EssenceCharge.CODEC, charge);
        output.putInt("progress", progress);
        output.putInt("duration", duration);
        output.putBoolean("completed_output", completedOutput);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        link.load(input);
        items.clear();
        ContainerHelper.loadAllItems(input, items);
        charge = input.read("charge", EssenceCharge.CODEC).orElse(EssenceCharge.EMPTY);
        progress = input.getIntOr("progress", 0);
        duration = input.getIntOr("duration", 0);
        completedOutput = input.getBooleanOr("completed_output", false);
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
