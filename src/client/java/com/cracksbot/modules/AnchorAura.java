package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class AnchorAura extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 2.0, 6.0, 0.5));

    private long lastAction = 0;

    public AnchorAura() {
        super("AnchorAura", "Respawn Anchor bomb (Overworld/End)", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        // Anchors only explode outside the Nether
        if (mc.level.dimension() == net.minecraft.world.level.Level.NETHER) return;

        long now = System.currentTimeMillis();
        if (now - lastAction < 200) return;

        LocalPlayer p = mc.player;
        Player target = findTarget(mc);
        if (target == null) return;

        BlockPos targetPos = target.blockPosition();
        int r = range.getValue().intValue();

        // Look for charged anchors to detonate
        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = targetPos.offset(x, y, z);
                    if (mc.level.getBlockState(pos).is(Blocks.RESPAWN_ANCHOR)) {
                        int charge = mc.level.getBlockState(pos).getValue(RespawnAnchorBlock.CHARGE);
                        if (charge > 0) {
                            double dist = p.position().distanceTo(Vec3.atCenterOf(pos));
                            if (dist <= range.getValue()) {
                                // Right click to set spawn = explode outside nether
                                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                                lastAction = now;
                                return;
                            }
                        }
                    }
                }
            }
        }

        // Place anchor + charge with glowstone + detonate
        int anchorSlot = findItemSlot(p, Items.RESPAWN_ANCHOR);
        int glowSlot = findItemSlot(p, Items.GLOWSTONE);
        if (anchorSlot == -1 || glowSlot == -1) return;

        // Find placement spot near target
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = targetPos.offset(x, 0, z);
                if (!mc.level.getBlockState(pos).isAir()) continue;
                if (mc.level.getBlockState(pos.below()).isAir()) continue;

                double dist = p.position().distanceTo(Vec3.atCenterOf(pos));
                if (dist > range.getValue()) continue;

                int prev = p.getInventory().getSelectedSlot();

                // Place anchor
                p.getInventory().setSelectedSlot(anchorSlot);
                BlockHitResult placeHit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos.below(), false);
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, placeHit);

                // Charge with glowstone
                p.getInventory().setSelectedSlot(glowSlot);
                BlockHitResult chargeHit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, chargeHit);

                // Detonate (right click without glowstone)
                p.getInventory().setSelectedSlot(prev);
                BlockHitResult detonateHit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, detonateHit);

                lastAction = now;
                return;
            }
        }
    }

    private int findItemSlot(LocalPlayer p, net.minecraft.world.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }

    private Player findTarget(Minecraft mc) {
        Player nearest = null;
        double nearestDist = 8.0;
        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive()) continue;
            double dist = mc.player.distanceTo(player);
            if (dist < nearestDist) { nearestDist = dist; nearest = player; }
        }
        return nearest;
    }
}
