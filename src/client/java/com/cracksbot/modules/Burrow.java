package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Burrow extends HackModule {
    private boolean done = false;

    public Burrow() {
        super("Burrow", "Glitch into a block at your feet", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onEnable() {
        done = false;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;
        if (done) return;

        LocalPlayer p = mc.player;
        if (!p.onGround()) return;

        int obsSlot = findObsidianSlot(p);
        if (obsSlot == -1) return;

        BlockPos feetPos = p.blockPosition();

        // Step 1: Jump player up via packet (fake position)
        double x = p.getX();
        double y = p.getY();
        double z = p.getZ();

        // Send position packet that puts us above
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y + 1.16, z, false, false));

        // Step 2: Place obsidian at feet
        int prev = p.getInventory().getSelectedSlot();
        p.getInventory().setSelectedSlot(obsSlot);

        for (Direction dir : Direction.values()) {
            BlockPos support = feetPos.relative(dir);
            if (!mc.level.getBlockState(support).isAir()) {
                BlockHitResult hit = new BlockHitResult(
                    Vec3.atCenterOf(feetPos), dir.getOpposite(), support, false
                );
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                break;
            }
        }

        p.getInventory().setSelectedSlot(prev);

        // Step 3: Rubber band back down into the block
        mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, true, false));

        done = true;
        toggle(); // Auto-disable
    }

    private int findObsidianSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getItem(i).is(Items.OBSIDIAN)) return i;
        }
        return -1;
    }
}
