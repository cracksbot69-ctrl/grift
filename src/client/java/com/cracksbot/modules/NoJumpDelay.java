package com.cracksbot.modules;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class NoJumpDelay extends HackModule {
    public NoJumpDelay() {
        super("NoJumpDelay", "Faster jump reset", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        // Reset jump delay to 0 every tick so player can spam jump
        // Uses reflection-free approach: just make the player jump-ready
        if (mc.player.onGround() && mc.options.keyJump.isDown()) {
            mc.player.jumpFromGround();
        }
    }
}
