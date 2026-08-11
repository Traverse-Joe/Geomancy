package com.traverse.geomancy.block.entity;

import java.util.Comparator;
import java.util.ArrayList;
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
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;

import com.traverse.geomancy.recipe.HearthSynthesisInput;
import com.traverse.geomancy.recipe.HearthSynthesisRecipe;
import com.traverse.geomancy.registry.ModBlockEntities;
import com.traverse.geomancy.registry.ModRecipes;
import com.traverse.geomancy.resonance.ResonanceStorage;

public class ResonantHearthBlockEntity extends BlockEntity implements ResonanceStorage {
    public static final int MAX_INGREDIENTS = 8;

    // Sized to comfortably cover the costliest current recipe (750, the Lithic Pickaxe),
    // with headroom - not a battery, but enough that a receiver bound to the Hearth can
    // actually bank up for a real synthesis rather than only ever passing resonance through.
    private static final int BUFFER_CAPACITY = 1_000;

    private final NonNullList<ItemStack> ingredients = NonNullList.withSize(MAX_INGREDIENTS, ItemStack.EMPTY);
    private ItemStack focus = ItemStack.EMPTY;
    private int buffer;

    public ResonantHearthBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANT_HEARTH.get(), pos, state);
    }

    public void activate(Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB basin = new AABB(worldPosition).expandTowards(0.0, 1.0, 0.0);
        List<ItemEntity> entities = serverLevel.getEntitiesOfClass(ItemEntity.class, basin,
                entity -> entity.isAlive() && !entity.getItem().isEmpty());
        entities.sort(Comparator.comparing(Entity -> Entity.getUUID().toString()));
        List<ItemStack> inputItems = new ArrayList<>();
        ingredients.stream().filter(stack -> !stack.isEmpty()).map(ItemStack::copy).forEach(inputItems::add);
        entities.stream().map(entity -> entity.getItem().copy()).forEach(inputItems::add);
        HearthSynthesisInput input = new HearthSynthesisInput(inputItems, !focus.isEmpty());
        RecipeHolder<HearthSynthesisRecipe> holder = serverLevel.recipeAccess()
                .getRecipeFor(ModRecipes.HEARTH_SYNTHESIS_TYPE.get(), input, serverLevel)
                .orElse(null);
        if (holder == null) {
            feedback(player, "block.geomancy.resonant_hearth.no_recipe");
            fail(serverLevel);
            return;
        }

        HearthSynthesisRecipe recipe = holder.value();
        int resonanceCost = recipe.cost().amount();
        ResonanceStorage storage = resonanceSource(serverLevel, resonanceCost);
        if (storage == null) {
            feedback(player, "block.geomancy.resonant_hearth.no_resonance", resonanceCost);
            fail(serverLevel);
            return;
        }

        if (storage.extractResonance(resonanceCost, false) != resonanceCost) {
            throw new IllegalStateException("Simulated Hearth resonance extraction did not match actual extraction");
        }
        entities.forEach(ItemEntity::discard);
        ingredients.clear();
        sync();

        ItemStack output = recipe.assemble(input);
        ItemEntity result = new ItemEntity(serverLevel, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1,
                worldPosition.getZ() + 0.5, output);
        result.setDefaultPickUpDelay();
        result.setDeltaMovement(0.0, 0.16, 0.0);
        serverLevel.addFreshEntity(result);
        serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2F, 1.4F);
        serverLevel.sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8,
                worldPosition.getZ() + 0.5, 36, 0.6, 0.12, 0.6, 0.2);
        feedback(player, "block.geomancy.resonant_hearth.success");
    }

    public boolean addIngredient(ItemStack held) {
        for (int slot = 0; slot < ingredients.size(); slot++) {
            if (ingredients.get(slot).isEmpty()) {
                ingredients.set(slot, held.split(1));
                sync();
                return true;
            }
        }
        return false;
    }

    public boolean installFocus(ItemStack held) {
        if (!focus.isEmpty() || !held.is(com.traverse.geomancy.registry.ModItems.RESONANT_AMETHYST_FOCUS.get())) {
            return false;
        }
        focus = held.split(1);
        sync();
        return true;
    }

    public ItemStack removeFocus() {
        ItemStack removed = focus;
        focus = ItemStack.EMPTY;
        sync();
        return removed;
    }

    public ItemStack focus() {
        return focus;
    }

    public ItemStack removeLastIngredient() {
        for (int slot = ingredients.size() - 1; slot >= 0; slot--) {
            if (!ingredients.get(slot).isEmpty()) {
                ItemStack removed = ingredients.get(slot);
                ingredients.set(slot, ItemStack.EMPTY);
                sync();
                return removed;
            }
        }
        return ItemStack.EMPTY;
    }

    public List<ItemStack> ingredients() {
        return ingredients;
    }

    public void dropIngredients(ServerLevel level) {
        for (ItemStack stack : ingredients) {
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8,
                        worldPosition.getZ() + 0.5, stack.copy()));
            }
        }
        ingredients.clear();
        if (!focus.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5, focus.copy()));
            focus = ItemStack.EMPTY;
        }
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    // Checks its own receiver-fed buffer first, then falls back to adjacent storage - a
    // receiver mounted on the Hearth's face inserts here through the same generic
    // ResonanceStorage path every other receiver target uses.
    private ResonanceStorage resonanceSource(ServerLevel level, int required) {
        if (extractResonance(required, true) == required) {
            return this;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ResonanceStorage storage
                    && storage.extractResonance(required, true) == required) {
                return storage;
            }
        }
        return null;
    }

    @Override
    public int resonance() {
        return buffer;
    }

    @Override
    public int capacity() {
        return BUFFER_CAPACITY;
    }

    @Override
    public int insertResonance(int amount, boolean simulate) {
        int accepted = Math.min(Math.max(amount, 0), capacity() - buffer);
        if (!simulate && accepted > 0) {
            buffer += accepted;
            sync();
        }
        return accepted;
    }

    @Override
    public int extractResonance(int amount, boolean simulate) {
        int extracted = Math.min(Math.max(amount, 0), buffer);
        if (!simulate && extracted > 0) {
            buffer -= extracted;
            sync();
        }
        return extracted;
    }

    private void fail(ServerLevel level) {
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_FALL, SoundSource.BLOCKS, 0.8F, 0.6F);
        level.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 0.8,
                worldPosition.getZ() + 0.5, 8, 0.25, 0.05, 0.25, 0.01);
    }

    private static void feedback(Player player, String key, Object... args) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(key, args), true);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, ingredients, true);
        output.store("focus", ItemStack.OPTIONAL_CODEC, focus);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ingredients.clear();
        ContainerHelper.loadAllItems(input, ingredients);
        focus = input.read("focus", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
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
