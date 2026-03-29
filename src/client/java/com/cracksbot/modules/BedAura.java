package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class BedAura extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 2.0, 6.0, 0.5));
    private final NumberSetting breakSpeed = addSetting(new NumberSetting("Speed", 5, 1, 20, 1));

    private long lastAction = 0;

    public BedAura() {
        super("BedAura", "Place & break beds (Nether/End)", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        // Only works in Nether/End where beds explode
        if (mc.level.dimension() == net.minecraft.world.level.Level.OVERWORLD) return;

        long now = System.currentTimeMillis();
        long interval = (long) (1000.0 / breakSpeed.getValue());
        if (now - lastAction < interval) return;

        LocalPlayer p = mc.player;

        // First: try to break existing beds near target
        Player target = findTarget(mc);
        if (target == null) return;

        BlockPos targetPos = target.blockPosition();
        int r = range.getValue().intValue();

        // Look for placed beds to detonate
        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = targetPos.offset(x, y, z);
                    if (mc.level.getBlockState(pos).getBlock() instanceof BedBlock) {
                        double dist = p.position().distanceTo(Vec3.atCenterOf(pos));
                        if (dist <= range.getValue()) {
                            // Right click to "sleep" = explode
                            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                            mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                            lastAction = now;
                            return;
                        }
                    }
                }
            }
        }

        // Place a bed near target
        int bedSlot = findBedSlot(p);
        if (bedSlot == -1) return;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos pos = targetPos.offset(x, 0, z);
                if (!mc.level.getBlockState(pos).isAir()) continue;
                if (mc.level.getBlockState(pos.below()).isAir()) continue;
                // Need adjacent air for bed foot
                if (!mc.level.getBlockState(pos.north()).isAir() &&
                    !mc.level.getBlockState(pos.south()).isAir() &&
                    !mc.level.getBlockState(pos.east()).isAir() &&
                    !mc.level.getBlockState(pos.west()).isAir()) continue;

                double dist = p.position().distanceTo(Vec3.atCenterOf(pos));
                if (dist > range.getValue()) continue;

                int prev = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(bedSlot);
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos.below(), false);
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                p.getInventory().setSelectedSlot(prev);
                lastAction = now;
                return;
            }
        }
    }

    private int findBedSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            Item item = p.getInventory().getItem(i).getItem();
            String name = item.toString().toLowerCase();
            if (name.contains("bed")) return i;
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
