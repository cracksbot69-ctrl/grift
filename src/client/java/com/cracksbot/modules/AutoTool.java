package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class AutoTool extends HackModule {
    public AutoTool() {
        super("AutoTool", "Auto switch to best tool", ModuleCategory.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        if (!(mc.hitResult instanceof BlockHitResult bhr)) return;
        if (mc.hitResult.getType() == HitResult.Type.MISS) return;
        if (!mc.options.keyAttack.isDown()) return;

        LocalPlayer p = mc.player;
        BlockState state = mc.level.getBlockState(bhr.getBlockPos());

        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (bestSlot != -1) {
            p.getInventory().setSelectedSlot(bestSlot);
        }
    }
}
