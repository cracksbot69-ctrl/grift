package com.cracksbot.modules;

import com.cracksbot.CracksBotClient;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class SelfDestruct extends HackModule {
    public SelfDestruct() {
        super("SelfDestruct", "Disable all modules (panic)", ModuleCategory.MISC, GLFW.GLFW_KEY_END, false);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        for (HackModule m : CracksBotClient.INSTANCE.getModules()) {
            if (m != this && m.isEnabled()) {
                m.setEnabled(false);
            }
        }
        CracksBotClient.notify(mc, "\u00A7cALL MODULES DISABLED");
        setEnabled(false);
    }

    @Override
    public void onTick(Minecraft mc) {}
}
