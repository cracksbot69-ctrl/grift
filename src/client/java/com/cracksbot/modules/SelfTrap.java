package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class SelfTrap extends HackModule {
    public SelfTrap() {
        super("SelfTrap", "Obsidian over your head", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        int obsSlot = findObsidianSlot(p);
        if (obsSlot == -1) return;

        BlockPos head = p.blockPosition().above(2);
        if (!mc.level.getBlockState(head).isAir()) return;

        // Find support
        for (Direction dir : Direction.values()) {
            BlockPos support = head.relative(dir);
            if (!mc.level.getBlockState(support).isAir()) {
                int prevSlot = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(obsSlot);
                BlockHitResult hit = new BlockHitResult(
                    Vec3.atCenterOf(head), dir.getOpposite(), support, false
                );
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                p.getInventory().setSelectedSlot(prevSlot);
                return;
            }
        }
    }

    private int findObsidianSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(Items.OBSIDIAN)) return i;
        }
        return -1;
    }
}
