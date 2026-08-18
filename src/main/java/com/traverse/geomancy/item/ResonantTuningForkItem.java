package com.traverse.geomancy.item;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.traverse.geomancy.block.ResonanceEmitterBlock;
import com.traverse.geomancy.block.entity.GeodeJarBlockEntity;
import com.traverse.geomancy.block.entity.ResonanceEmitterBlockEntity;
import com.traverse.geomancy.block.entity.TectonicNodeBlockEntity;
import com.traverse.geomancy.block.ResonanceReceiverBlock;
import com.traverse.geomancy.registry.ModDataComponents;
import com.traverse.geomancy.resonance.BatterySize;
import com.traverse.geomancy.resonance.BindResult;
import com.traverse.geomancy.resonance.ResonanceStorage;

// Replaces the Geomancer's Tuning Hammer. Strikes the Hearth (handled by
// ResonantHearthBlock, which intercepts the click before this item sees it) and binds
// emitters to receivers: click the emitter, then the receiver. Shift-click an emitter to
// clear it. Shift-use in air toggles binding mode, which is what makes link lines render.
public class ResonantTuningForkItem extends Item {
    public ResonantTuningForkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            boolean bindingMode = !stack.getOrDefault(ModDataComponents.BINDING_MODE.get(), false);
            stack.set(ModDataComponents.BINDING_MODE.get(), bindingMode);
            feedback(player, bindingMode ? "item.geomancy.resonant_tuning_fork.binding_mode_on"
                    : "item.geomancy.resonant_tuning_fork.binding_mode_off");
        } else {
            stack.remove(ModDataComponents.LINKED_SOURCE.get());
            feedback(player, "item.geomancy.resonant_tuning_fork.source_cleared");
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clicked = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockEntity clickedBe = level.getBlockEntity(clicked);

        if (player != null && player.isShiftKeyDown() && clickedBe instanceof ResonanceEmitterBlockEntity emitter) {
            emitter.clear();
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_FALL, 1.0F, 0.8F);
            feedback(player, "item.geomancy.resonant_tuning_fork.unlinked");
            return InteractionResult.SUCCESS_SERVER;
        }

        if (player != null && player.isShiftKeyDown() && clickedBe instanceof GeodeJarBlockEntity jar
                && jar.node() != null) {
            jar.clearNode();
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_FALL, 1.0F, 0.8F);
            feedback(player, "item.geomancy.resonant_tuning_fork.unlinked");
            return InteractionResult.SUCCESS_SERVER;
        }

        // Emitters and nodes are both link sources; what the second click lands on decides
        // which kind of link is being made.
        if (clickedBe instanceof ResonanceEmitterBlockEntity || clickedBe instanceof TectonicNodeBlockEntity) {
            stack.set(ModDataComponents.LINKED_SOURCE.get(), clicked);
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_HIT, 1.0F, 1.2F);
            feedback(player, "item.geomancy.resonant_tuning_fork.selected");
            return InteractionResult.SUCCESS_SERVER;
        }

        BlockPos sourcePos = stack.get(ModDataComponents.LINKED_SOURCE.get());
        if (sourcePos == null) {
            return InteractionResult.PASS;
        }

        if (clickedBe instanceof GeodeJarBlockEntity jar
                && level.getBlockEntity(sourcePos) instanceof TectonicNodeBlockEntity) {
            BindResult result = jar.bindNode(level, sourcePos);
            if (result != BindResult.OK) {
                feedback(player, bindFailureKey(result));
                return fail(level, clicked);
            }
            stack.remove(ModDataComponents.LINKED_SOURCE.get());
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.1F);
            feedback(player, "item.geomancy.resonant_tuning_fork.attuned");
            return InteractionResult.SUCCESS_SERVER;
        }

        if (level.getBlockState(clicked).getBlock() instanceof ResonanceReceiverBlock) {
            BindResult result = bind(level, sourcePos, clicked);
            if (result != BindResult.OK) {
                feedback(player, bindFailureKey(result));
                return fail(level, clicked);
            }
            stack.remove(ModDataComponents.LINKED_SOURCE.get());
            play(level, clicked, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.4F);
            feedback(player, "item.geomancy.resonant_tuning_fork.bound");
            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }

    private static BindResult bind(Level level, BlockPos sourcePos, BlockPos receiverPos) {
        if (!(level.getBlockEntity(sourcePos) instanceof ResonanceEmitterBlockEntity emitter)) {
            return BindResult.SOURCE_GONE;
        }
        if (!(level.getBlockState(receiverPos).getBlock() instanceof ResonanceReceiverBlock)) {
            return BindResult.NOT_A_RECEIVER;
        }
        Direction facing = level.getBlockState(sourcePos).getValue(ResonanceEmitterBlock.FACING);
        BlockPos hostPos = sourcePos.relative(facing.getOpposite());
        double reach = level.getBlockEntity(hostPos) instanceof ResonanceStorage storage
                ? storage.emitterReach()
                : BatterySize.SMALL.reach();
        if (sourcePos.distSqr(receiverPos) > reach * reach) {
            return BindResult.TOO_FAR;
        }
        emitter.bind(receiverPos);
        return BindResult.OK;
    }

    private static String bindFailureKey(BindResult result) {
        String suffix = switch (result) {
            case OK -> throw new IllegalStateException("OK is not a failure");
            case NOT_A_RECEIVER -> "not_a_receiver";
            case TOO_FAR -> "out_of_range";
            case SOURCE_GONE -> "source_gone";
        };
        return "item.geomancy.resonant_tuning_fork." + suffix;
    }

    private static void play(Level level, BlockPos pos, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static InteractionResult fail(Level level, BlockPos pos) {
        play(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME, 0.7F, 0.5F);
        return InteractionResult.FAIL;
    }

    private static void feedback(@Nullable Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(key), true);
        }
    }
}
