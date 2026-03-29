package com.cracksbot;

import com.cracksbot.gui.ClickGui;
import com.cracksbot.modules.*;
import com.cracksbot.hud.ModuleHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CracksBotClient implements ClientModInitializer {
    public static CracksBotClient INSTANCE;
    public static final String VERSION = "2.0";

    private final List<HackModule> modules = new ArrayList<>();
    private KeyMapping guiToggle;
    private KeyMapping hudToggle;
    private boolean hudVisible = true;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        // Combat
        modules.add(new KillAura());
        modules.add(new AutoCrystal());
        modules.add(new CrystalAura());
        modules.add(new Criticals());
        modules.add(new Velocity());
        modules.add(new AutoTotem());
        modules.add(new AutoGapple());
        modules.add(new Aimbot());
        modules.add(new Reach());
        modules.add(new AutoTrap());
        modules.add(new SelfTrap());
        modules.add(new BedAura());
        modules.add(new AnchorAura());
        modules.add(new Burrow());
        modules.add(new AutoShieldBreaker());
        modules.add(new TriggerBot());
        modules.add(new HitboxExpand());
        modules.add(new NoJumpDelay());
        modules.add(new Predict());
        modules.add(new DoubleHand());

        // Movement
        modules.add(new Sprint());
        modules.add(new Flight());
        modules.add(new Speed());
        modules.add(new NoFall());
        modules.add(new Jesus());
        modules.add(new Step());
        modules.add(new AutoPearl());
        modules.add(new WTap());

        // Render
        modules.add(new ESP());
        modules.add(new Tracers());
        modules.add(new Fullbright());
        modules.add(new Xray());
        modules.add(new Freecam());
        modules.add(new CrystalESP());
        modules.add(new NameTags());
        modules.add(new NoRender());

        // Player
        modules.add(new AutoEat());
        modules.add(new Surround());
        modules.add(new ChestStealer());
        modules.add(new AutoFish());
        modules.add(new AutoTool());
        modules.add(new AutoPot());
        modules.add(new AutoEXP());

        // World
        modules.add(new Scaffold());
        modules.add(new Nuker());
        modules.add(new Timer());
        modules.add(new HoleFiller());
        modules.add(new CobwebPlacer());
        modules.add(new FastPlace());

        // Misc
        modules.add(new AntiBot());
        modules.add(new AutoReconnect());
        modules.add(new TotemPopCounter());
        modules.add(new SelfDestruct());
        modules.add(new FakeLag());
        modules.add(new FriendSystem());
        modules.add(new NameHider());
        modules.add(new PracticeArena());

        // ClickGUI keybind (RIGHT_SHIFT)
        guiToggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "cracksbot.gui", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.GAMEPLAY
        ));

        // HUD toggle
        hudToggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "cracksbot.hud", GLFW.GLFW_KEY_F9, KeyMapping.Category.GAMEPLAY
        ));

        for (HackModule m : modules) m.registerKeybinding();

        // Tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiToggle.consumeClick()) {
                if (client.screen instanceof ClickGui) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new ClickGui());
                }
            }
            while (hudToggle.consumeClick()) {
                hudVisible = !hudVisible;
                notify(client, "HUD " + (hudVisible ? "\u00A7aON" : "\u00A7cOFF"));
            }
            for (HackModule m : modules) {
                m.checkKeybind();
                if (m.isEnabled()) m.onTick(client);
            }
        });

        // HUD
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (hudVisible) ModuleHud.render(drawContext, modules);
        });

        // ESP world rendering
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            if (INSTANCE == null) return;
            ESP esp = INSTANCE.get(ESP.class);
            if (esp != null && esp.isEnabled()) {
                esp.onWorldRender(ctx, Minecraft.getInstance());
            }
        });

        System.out.println("[Grift] v" + VERSION + " loaded! " + modules.size() + " modules. Press RIGHT_SHIFT for GUI.");
    }

    public List<HackModule> getModules() { return modules; }

    @SuppressWarnings("unchecked")
    public <T extends HackModule> T get(Class<T> c) {
        for (HackModule m : modules) if (c.isInstance(m)) return (T) m;
        return null;
    }

    public static void notify(Minecraft mc, String msg) {
        if (mc.player != null)
            mc.player.displayClientMessage(Component.literal("\u00A75[Grift] \u00A7f" + msg), true);
    }
}
