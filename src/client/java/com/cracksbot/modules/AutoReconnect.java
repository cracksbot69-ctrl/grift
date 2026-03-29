package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AutoReconnect extends HackModule {
    private final NumberSetting delay = addSetting(new NumberSetting("Delay", 5.0, 1.0, 30.0, 1.0));

    public AutoReconnect() {
        super("AutoReconnect", "Auto reconnect on kick", ModuleCategory.MISC, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
