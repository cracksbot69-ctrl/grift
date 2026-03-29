package com.cracksbot.modules;

import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class Predict extends HackModule {
    private final NumberSetting ticks = addSetting(new NumberSetting("Ticks", 3, 1, 10, 1));

    private final Map<String, Vec3> lastPositions = new HashMap<>();

    public Predict() {
        super("Predict", "Predict enemy movement", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            String name = player.getName().getString();
            lastPositions.put(name, player.position());
        }
    }

    /**
     * Get predicted position of target player.
     * Used by AutoCrystal for better placement.
     */
    public Vec3 getPredictedPos(Player target) {
        String name = target.getName().getString();
        Vec3 lastPos = lastPositions.get(name);
        Vec3 currentPos = target.position();

        if (lastPos == null) return currentPos;

        // Calculate velocity and extrapolate
        Vec3 velocity = currentPos.subtract(lastPos);
        int ticksAhead = ticks.getValue().intValue();
        return currentPos.add(velocity.scale(ticksAhead));
    }
}
