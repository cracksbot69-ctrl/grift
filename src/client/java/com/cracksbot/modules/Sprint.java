package com.cracksbot.modules;

import com.cracksbot.settings.BoolSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Sprint extends HackModule {
    private final BoolSetting omni = addSetting(new BoolSetting("OmniSprint", false));

    public Sprint() {
        super("Sprint", "Always sprint at max speed", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_V, true);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.player.isUsingItem()) return;
        boolean moving = omni.getValue()
            ? (mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
               || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown())
            : mc.options.keyUp.isDown();
        if (moving && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
