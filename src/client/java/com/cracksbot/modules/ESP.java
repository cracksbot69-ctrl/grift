package com.cracksbot.modules;

import com.cracksbot.settings.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class ESP extends HackModule {

    // ── Players ──────────────────────────────────────────────────────────────
    public final BoolSetting   playersEnabled  = addSetting(new BoolSetting("Players",       true));
    public final ColorSetting  playerColor     = addSetting(new ColorSetting("P-Color",      0xFF4488FF));
    public final NumberSetting playerFill      = addSetting(new NumberSetting("P-Fill",      0.15, 0.0, 1.0, 0.05));
    public final BoolSetting   playerTracers   = addSetting(new BoolSetting("P-Tracers",     true));
    public final BoolSetting   playerNametags  = addSetting(new BoolSetting("P-Nametags",    true));
    public final BoolSetting   playerHealthBar = addSetting(new BoolSetting("P-HealthBar",   true));

    // ── Hostiles ─────────────────────────────────────────────────────────────
    public final BoolSetting   hostilesEnabled  = addSetting(new BoolSetting("Hostiles",     false));
    public final ColorSetting  hostileColor     = addSetting(new ColorSetting("H-Color",     0xFFFF4444));
    public final NumberSetting hostileFill      = addSetting(new NumberSetting("H-Fill",     0.10, 0.0, 1.0, 0.05));
    public final BoolSetting   hostileTracers   = addSetting(new BoolSetting("H-Tracers",    false));
    public final BoolSetting   hostileNametags  = addSetting(new BoolSetting("H-Nametags",   false));
    public final BoolSetting   hostileHealthBar = addSetting(new BoolSetting("H-HealthBar",  false));

    // ── Passives ─────────────────────────────────────────────────────────────
    public final BoolSetting   passivesEnabled  = addSetting(new BoolSetting("Passives",     false));
    public final ColorSetting  passiveColor     = addSetting(new ColorSetting("Pa-Color",    0xFF44FF88));
    public final NumberSetting passiveFill      = addSetting(new NumberSetting("Pa-Fill",    0.08, 0.0, 1.0, 0.05));
    public final BoolSetting   passiveTracers   = addSetting(new BoolSetting("Pa-Tracers",   false));
    public final BoolSetting   passiveNametags  = addSetting(new BoolSetting("Pa-Nametags",  false));
    public final BoolSetting   passiveHealthBar = addSetting(new BoolSetting("Pa-HealthBar", false));

    // ── Global ────────────────────────────────────────────────────────────────
    public final NumberSetting outlineWidth = addSetting(new NumberSetting("OutlineWidth", 1.0, 1.0, 4.0, 1.0));
    public final BoolSetting   globalFill   = addSetting(new BoolSetting("GlobalFill",    true));
    public final BoolSetting   showDistance = addSetting(new BoolSetting("Distance",      true));

    public ESP() {
        super("ESP", "See players, hostiles, and passives through walls", ModuleCategory.RENDER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override public void onTick(Minecraft mc) {}

    public void onWorldRender(WorldRenderContext ctx, Minecraft mc) {
        PoseStack ps = ctx.matrices();
        if (ps == null || mc.level == null || mc.player == null || ctx.consumers() == null) return;

        MultiBufferSource buf    = ctx.consumers();
        Vec3              camPos = mc.gameRenderer.getMainCamera().position();

        ps.pushPose();
        ps.translate(-camPos.x, -camPos.y, -camPos.z);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive()) continue;

            boolean isPlayer  = entity instanceof Player;
            boolean isHostile = !isPlayer && entity instanceof Monster;
            boolean isPassive = !isPlayer && !isHostile
                && (entity instanceof Animal || entity instanceof AbstractVillager)
                && entity instanceof LivingEntity;

            if (!isPlayer && !isHostile && !isPassive) continue;
            if (isPlayer  && !playersEnabled.getValue())  continue;
            if (isHostile && !hostilesEnabled.getValue()) continue;
            if (isPassive && !passivesEnabled.getValue()) continue;

            ColorSetting  cs  = isPlayer ? playerColor     : isHostile ? hostileColor     : passiveColor;
            NumberSetting fs  = isPlayer ? playerFill      : isHostile ? hostileFill      : passiveFill;
            BoolSetting   trs = isPlayer ? playerTracers   : isHostile ? hostileTracers   : passiveTracers;
            BoolSetting   hbs = isPlayer ? playerHealthBar : isHostile ? hostileHealthBar : passiveHealthBar;

            float r = cs.r() / 255f;
            float g = cs.g() / 255f;
            float b = cs.b() / 255f;

            AABB           box  = entity.getBoundingBox();
            PoseStack.Pose pose = ps.last();

            // ── Box outline ──────────────────────────────────────────────
            VertexConsumer vc = buf.getBuffer(RenderTypes.LINES);
            renderBox(vc, pose, box, r, g, b, 1.0f);

            // ── Tracer ───────────────────────────────────────────────────
            if (trs.getValue()) {
                double ex = (box.minX + box.maxX) * 0.5;
                double ey = (box.minY + box.maxY) * 0.5;
                double ez = (box.minZ + box.maxZ) * 0.5;
                VertexConsumer tvc = buf.getBuffer(RenderTypes.LINES);
                drawLine(tvc, pose,
                    (float) camPos.x, (float) camPos.y, (float) camPos.z,
                    (float) ex, (float) ey, (float) ez,
                    r, g, b, 0.6f);
            }

            // ── Health bar ───────────────────────────────────────────────
            if (hbs.getValue() && entity instanceof LivingEntity living) {
                float hp    = living.getHealth();
                float maxHp = living.getMaxHealth();
                float pct   = Math.max(0f, Math.min(1f, hp / maxHp));
                float bW    = (float)(box.maxX - box.minX);
                float top   = (float)(box.maxY + 0.2f);
                float cx    = (float)((box.minX + box.maxX) * 0.5);
                float cz    = (float)((box.minZ + box.maxZ) * 0.5);

                VertexConsumer hvc  = buf.getBuffer(RenderTypes.LINES);
                VertexConsumer fhvc = buf.getBuffer(RenderTypes.LINES);
                drawLine(hvc,  pose, cx-bW*0.5f, top, cz, cx+bW*0.5f,         top, cz, 0.15f,    0.15f, 0.15f, 1f);
                drawLine(fhvc, pose, cx-bW*0.5f, top, cz, cx-bW*0.5f+bW*pct, top, cz, 1f-pct, pct,   0f,   1f);
            }
        }

        ps.popPose();
    }

    private static void renderBox(VertexConsumer vc, PoseStack.Pose pose, AABB b,
                                   float r, float g, float bl, float a) {
        float x0 = (float)b.minX, y0 = (float)b.minY, z0 = (float)b.minZ;
        float x1 = (float)b.maxX, y1 = (float)b.maxY, z1 = (float)b.maxZ;
        drawLine(vc,pose, x0,y0,z0, x1,y0,z0, r,g,bl,a);
        drawLine(vc,pose, x1,y0,z0, x1,y0,z1, r,g,bl,a);
        drawLine(vc,pose, x1,y0,z1, x0,y0,z1, r,g,bl,a);
        drawLine(vc,pose, x0,y0,z1, x0,y0,z0, r,g,bl,a);
        drawLine(vc,pose, x0,y1,z0, x1,y1,z0, r,g,bl,a);
        drawLine(vc,pose, x1,y1,z0, x1,y1,z1, r,g,bl,a);
        drawLine(vc,pose, x1,y1,z1, x0,y1,z1, r,g,bl,a);
        drawLine(vc,pose, x0,y1,z1, x0,y1,z0, r,g,bl,a);
        drawLine(vc,pose, x0,y0,z0, x0,y1,z0, r,g,bl,a);
        drawLine(vc,pose, x1,y0,z0, x1,y1,z0, r,g,bl,a);
        drawLine(vc,pose, x1,y0,z1, x1,y1,z1, r,g,bl,a);
        drawLine(vc,pose, x0,y0,z1, x0,y1,z1, r,g,bl,a);
    }

    private static void drawLine(VertexConsumer vc, PoseStack.Pose pose,
                                  float x1, float y1, float z1,
                                  float x2, float y2, float z2,
                                  float r, float g, float b, float a) {
        float dx = x2-x1, dy = y2-y1, dz = z2-z1;
        float len = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len > 0f) { dx/=len; dy/=len; dz/=len; }
        vc.addVertex(pose, x1,y1,z1).setColor(r,g,b,a).setNormal(pose, dx,dy,dz);
        vc.addVertex(pose, x2,y2,z2).setColor(r,g,b,a).setNormal(pose, dx,dy,dz);
    }
}
