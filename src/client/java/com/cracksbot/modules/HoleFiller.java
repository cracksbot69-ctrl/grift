package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class HoleFiller extends HackModule {
    private final NumberSetting range = addSetting(new NumberSetting("Range", 4.0, 2.0, 6.0, 0.5));

    public HoleFiller() {
        super("HoleFiller", "Fill holes near you", ModuleCategory.WORLD, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        LocalPlayer p = mc.player;

        int obsSlot = findObsidianSlot(p);
        if (obsSlot == -1) return;

        BlockPos center = p.blockPosition();
        int r = range.getValue().intValue();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -2; y <= 0; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (pos.equals(p.blockPosition())) continue;
                    if (!isHole(mc, pos)) continue;

                    // Fill it
                    for (Direction dir : Direction.values()) {
                        BlockPos support = pos.relative(dir);
                        if (!mc.level.getBlockState(support).isAir()) {
                            int prev = p.getInventory().getSelectedSlot();
                            p.getInventory().setSelectedSlot(obsSlot);
                            BlockHitResult hit = new BlockHitResult(
                                Vec3.atCenterOf(pos), dir.getOpposite(), support, false
                            );
                            mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                            p.getInventory().setSelectedSlot(prev);
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean isHole(Minecraft mc, BlockPos pos) {
        if (!mc.level.getBlockState(pos).isAir()) return false;
        if (!mc.level.getBlockState(pos.above()).isAir()) return false;

        int solidSides = 0;
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockState side = mc.level.getBlockState(pos.relative(dir));
            if (side.is(Blocks.OBSIDIAN) || side.is(Blocks.BEDROCK)) solidSides++;
        }
        BlockState below = mc.level.getBlockState(pos.below());
        boolean solidFloor = below.is(Blocks.OBSIDIAN) || below.is(Blocks.BEDROCK);

        return solidSides >= 3 && solidFloor;
    }

    private int findObsidianSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(Items.OBSIDIAN)) return i;
        }
        return -1;
    }
}
