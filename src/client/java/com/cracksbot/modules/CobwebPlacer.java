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

public class CobwebPlacer extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 2.0, 6.0, 0.5));

    public CobwebPlacer() {
        super("CobwebPlacer", "Trap enemies in cobwebs", ModuleCategory.WORLD, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        int webSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(Items.COBWEB)) { webSlot = i; break; }
        }
        if (webSlot == -1) return;

        Player target = findTarget(mc);
        if (target == null) return;

        BlockPos targetPos = target.blockPosition();
        if (!mc.level.getBlockState(targetPos).isAir()) return;

        for (Direction dir : Direction.values()) {
            BlockPos support = targetPos.relative(dir);
            if (!mc.level.getBlockState(support).isAir()) {
                int prev = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(webSlot);
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(targetPos), dir.getOpposite(), support, false);
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                p.getInventory().setSelectedSlot(prev);
                return;
            }
        }
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
}
