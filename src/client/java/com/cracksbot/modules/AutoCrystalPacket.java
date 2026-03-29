package com.cracksbot.modules;

import com.cracksbot.settings.*;
import com.cracksbot.utils.DamageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AutoCrystalPacket extends HackModule {

    private final NumberSetting placeSpeed   = addSetting(new NumberSetting("PlaceSpeed",   8,   1, 20, 1));
    private final NumberSetting breakSpeed   = addSetting(new NumberSetting("BreakSpeed",   12,  1, 20, 1));
    private final NumberSetting range        = addSetting(new NumberSetting("Range",        5.5, 2.0, 6.0, 0.1));
    private final NumberSetting wallRange    = addSetting(new NumberSetting("WallRange",    4.5, 1.0, 6.0, 0.1));
    private final NumberSetting minDamage    = addSetting(new NumberSetting("MinDamage",    2.0, 0.5, 20.0, 0.5));
    private final NumberSetting selfDmgLimit = addSetting(new NumberSetting("SelfDmgLimit", 8.0, 1.0, 20.0, 0.5));

    private long lastPlaceMs = 0;
    private long lastBreakMs = 0;

    public AutoCrystalPacket() {
        super("ACP", "Packet AutoCrystal", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;
        LocalPlayer p = mc.player;
        long now = System.currentTimeMillis();

        Player target = findTarget(mc);
        if (target == null) return;

        // BREAK — sofort & reaktiv
        long breakInterval = (long)(1000.0 / breakSpeed.getValue());
        if (now - lastBreakMs >= breakInterval) {
            EndCrystal best = findBestCrystal(mc, target, p);
            if (best != null) {
                silentLookAt(mc, p, best.position());
                mc.getConnection().send(ServerboundInteractPacket.createAttackPacket(best, p.isShiftKeyDown()));
                p.swing(InteractionHand.MAIN_HAND);
                lastBreakMs = now;
            }
        }

        // PLACE — berechnet & strategisch
        long placeInterval = (long)(1000.0 / placeSpeed.getValue());
        if (now - lastPlaceMs >= placeInterval) {
            BlockPos bestPos = findBestPlace(mc, target, p);
            if (bestPos != null) {
                InteractionHand hand = getHandWithCrystal(p);
                if (hand == null) {
                    int slot = findCrystalSlot(p);
                    if (slot == -1) return;
                    p.getInventory().setSelectedSlot(slot);
                    hand = InteractionHand.MAIN_HAND;
                }
                Vec3 placeVec = Vec3.atCenterOf(bestPos.above());
                silentLookAt(mc, p, placeVec);
                mc.getConnection().send(new ServerboundUseItemOnPacket(
                    hand,
                    new BlockHitResult(placeVec, Direction.UP, bestPos, false),
                    (int) mc.player.level().getGameTime()
                ));
                lastPlaceMs = now;
            }
        }
    }

    private EndCrystal findBestCrystal(Minecraft mc, Player target, LocalPlayer self) {
        List<EndCrystal> crystals = mc.level.getEntitiesOfClass(
            EndCrystal.class, self.getBoundingBox().inflate(range.getValue())
        );
        EndCrystal best  = null;
        double bestScore = -Double.MAX_VALUE;

        for (EndCrystal c : crystals) {
            if (!c.isAlive()) continue;
            double dmgTarget = DamageUtil.calcExplosionDamage(c.position(), target);
            double dmgSelf   = DamageUtil.calcExplosionDamage(c.position(), self);

            if (dmgSelf > selfDmgLimit.getValue()) continue;
            if (self.getHealth() - dmgSelf <= 1.0) continue;

            double score = dmgTarget * 2 - dmgSelf;
            if (DamageUtil.getTotalHealth(target) < 8) score += 10; // FacePlace boost

            if (score > bestScore) { bestScore = score; best = c; }
        }
        return best;
    }

    private BlockPos findBestPlace(Minecraft mc, Player target, LocalPlayer self) {
        BlockPos center  = target.blockPosition();
        BlockPos bestPos = null;
        double bestScore = -Double.MAX_VALUE;

        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = center.offset(x, y, z);

                    // Basis muss Obsidian oder Bedrock sein
                    var block = mc.level.getBlockState(pos).getBlock();
                    if (!block.equals(Blocks.OBSIDIAN) && !block.equals(Blocks.BEDROCK)) continue;
                    if (!mc.level.isEmptyBlock(pos.above())) continue;
                    if (!mc.level.isEmptyBlock(pos.above(2))) continue;

                    // Nur Crystal/Player blockieren Platzierung
                    AABB checkBox = new AABB(pos.above());
                    boolean blocked = mc.level.getEntities((Entity) null, checkBox,
                        e -> e instanceof Player || e instanceof EndCrystal
                    ).stream().findAny().isPresent();
                    if (blocked) continue;

                    Vec3 crystalPos = Vec3.atCenterOf(pos.above());
                    double dist = self.position().distanceTo(crystalPos);
                    if (dist > range.getValue()) continue;

                    double dmgTarget = DamageUtil.calcExplosionDamage(crystalPos, target);
                    double dmgSelf   = DamageUtil.calcExplosionDamage(crystalPos, self);

                    if (dmgTarget < minDamage.getValue()) continue;
                    if (dmgSelf > selfDmgLimit.getValue()) continue;
                    if (self.getHealth() - dmgSelf <= 1.0) continue;

                    double score = dmgTarget * 2 - dmgSelf;
                    if (DamageUtil.getTotalHealth(target) < 8) score += 10;

                    if (score > bestScore) { bestScore = score; bestPos = pos; }
                }
            }
        }
        return bestPos;
    }

    /** Silent Rotation: Server sieht Rotation, Client dreht sich nicht sichtbar */
    private void silentLookAt(Minecraft mc, LocalPlayer p, Vec3 targetPos) {
        Vec3 eyes = p.getEyePosition(1.0f);
        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;
        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));
        mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, p.onGround(), p.horizontalCollision));
    }

    private InteractionHand getHandWithCrystal(LocalPlayer p) {
        if (p.getMainHandItem().is(Items.END_CRYSTAL)) return InteractionHand.MAIN_HAND;
        if (p.getOffhandItem().is(Items.END_CRYSTAL)) return InteractionHand.OFF_HAND;
        return null;
    }

    private int findCrystalSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++)
            if (p.getInventory().getItem(i).is(Items.END_CRYSTAL)) return i;
        return -1;
    }

    private Player findTarget(Minecraft mc) {
        LocalPlayer self = mc.player;
        Player best      = null;
        double bestScore = Double.MAX_VALUE;
        for (Player t : mc.level.players()) {
            if (t == self || !t.isAlive()) continue;
            double dist = self.distanceTo(t);
            if (dist > range.getValue()) continue;
            // Score = Distanz + HP*2 → niedrig = bestes Ziel
            double score = dist + t.getHealth() * 2;
            if (score < bestScore) { bestScore = score; best = t; }
        }
        return best;
    }
}
