package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Surround extends HackModule {
    private final BoolSetting autoCenter = addSetting(new BoolSetting("Center", true));
    private final NumberSetting blocksPerTick = addSetting(new NumberSetting("BPT", 4, 1, 8, 1));

    private static final int[][] OFFSETS = {
        {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1},
        {1, -1, 0}, {-1, -1, 0}, {0, -1, 1}, {0, -1, -1} // Below sides too
    };

    public Surround() {
        super("Surround", "Obsidian around feet", ModuleCategory.COMBAT, GLFW.GLFW_KEY_Z, false);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && autoCenter.getValue()) {
            // Center player on block
            double x = Math.floor(mc.player.getX()) + 0.5;
            double z = Math.floor(mc.player.getZ()) + 0.5;
            mc.player.setPos(x, mc.player.getY(), z);
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        LocalPlayer p = mc.player;

        int obsSlot = findSlot(p, Items.OBSIDIAN);
        if (obsSlot == -1) obsSlot = findSlot(p, Items.CRYING_OBSIDIAN);
        if (obsSlot == -1) obsSlot = findSlot(p, Items.ENDER_CHEST);
        if (obsSlot == -1) return;

        BlockPos feetPos = p.blockPosition();
        int prevSlot = p.getInventory().getSelectedSlot();
        int placed = 0;
        int maxPlace = blocksPerTick.getValue().intValue();

        for (int[] offset : OFFSETS) {
            if (placed >= maxPlace) break;
            BlockPos target = feetPos.offset(offset[0], offset[1], offset[2]);

            if (!mc.level.getBlockState(target).isAir() && !mc.level.getBlockState(target).canBeReplaced()) continue;

            // Find a solid neighbor to place against
            for (Direction dir : Direction.values()) {
                BlockPos support = target.relative(dir);
                if (!mc.level.getBlockState(support).isAir() && !mc.level.getBlockState(support).canBeReplaced()) {
                    p.getInventory().setSelectedSlot(obsSlot);
                    BlockHitResult hit = new BlockHitResult(
                        Vec3.atCenterOf(target), dir.getOpposite(), support, false
                    );
                    mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                    placed++;
                    break;
                }
            }
        }

        if (placed > 0) p.getInventory().setSelectedSlot(prevSlot);
    }

    private int findSlot(LocalPlayer p, Item item) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }
}
