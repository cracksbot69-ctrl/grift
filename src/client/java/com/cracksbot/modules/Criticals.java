package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.lwjgl.glfw.GLFW;

public class Criticals extends HackModule {
    private final ModeSetting mode = addSetting(new ModeSetting("Mode", "Packet", "Packet", "MiniJump"));

    public Criticals() {
        super("Criticals", "Force critical hits", ModuleCategory.COMBAT, GLFW.GLFW_KEY_J, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        // MiniJump mode: auto tiny jump when attacking
        if (mc.player == null || mc.getConnection() == null) return;
        if ("MiniJump".equals(mode.getValue())) {
            if (mc.player.onGround() && mc.options.keyAttack.isDown()) {
                mc.player.jumpFromGround();
            }
        }
    }

    /** Called from Mixin on attack, or can be called manually */
    public void onAttack(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null) return;
        if (!"Packet".equals(mode.getValue())) return;
        if (!mc.player.onGround()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // Fake jump packets for guaranteed crit
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 1.1e-5, z, false, false));
        mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, true, false));
    }
}
