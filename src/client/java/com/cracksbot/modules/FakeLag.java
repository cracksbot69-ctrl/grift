package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class FakeLag extends HackModule {
    private final NumberSetting delay = addSetting(new NumberSetting("Delay", 200, 50, 2000, 50));

    public FakeLag() {
        super("FakeLag", "Simulate lag", ModuleCategory.MISC, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
