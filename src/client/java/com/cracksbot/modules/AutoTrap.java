package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class AutoTrap extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 2.0, 6.0, 0.5));

    // Obsidian cage: sides + top
    private static final int[][] TRAP_OFFSETS = {
        {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
        {1, 1, 0}, {-1, 1, 0}, {0, 1, 1}, {0, 1, -1},
        {0, 2, 0}
    };

    public AutoTrap() {
        super("AutoTrap", "Trap players in obsidian", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        int obsSlot = findObsidianSlot(p);
        if (obsSlot == -1) return;

        Player target = findTarget(mc);
        if (target == null) return;

        BlockPos targetFeet = target.blockPosition();
        int prevSlot = p.getInventory().getSelectedSlot();

        for (int[] off : TRAP_OFFSETS) {
            BlockPos pos = targetFeet.offset(off[0], off[1], off[2]);
            if (!mc.level.getBlockState(pos).isAir()) continue;

            // Find support block
            Direction placeDir = findPlaceDirection(mc, pos);
            if (placeDir == null) continue;

            BlockPos support = pos.relative(placeDir);
            p.getInventory().setSelectedSlot(obsSlot);
            BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(pos), placeDir.getOpposite(), support, false
            );
            mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
            p.getInventory().setSelectedSlot(prevSlot);
            return; // One block per tick
        }
    }

    private Direction findPlaceDirection(Minecraft mc, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos adj = pos.relative(dir);
            if (!mc.level.getBlockState(adj).isAir()) return dir;
        }
        return null;
    }

    private Player findTarget(Minecraft mc) {
        Player nearest = null;
        double nearestDist = range.getValue();
        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive()) continue;
            double dist = mc.player.distanceTo(player);
            if (dist < nearestDist) { nearestDist = dist; nearest = player; }
        }
        return nearest;
    }

    private int findObsidianSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(Items.OBSIDIAN)) return i;
        }
        return -1;
    }
}
