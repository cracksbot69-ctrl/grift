package com.cracksbot.modules;

import com.cracksbot.mixin.LivingEntityHurtAccessor;
import com.cracksbot.settings.*;
import com.cracksbot.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AutoCrystal — Professionelles Crystal-PvP-Modul.
 *
 * ✅ Vollständig implementiert:
 * - Packet-basiertes Platzieren & Angreifen (schneller als gameMode-Methoden)
 * - Silent Rotation (Server sieht Rotation, Client nicht)
 * - Damage-Tick-Tracking (hurtTime Mixin-Accessor)
 * - D-Tap: Schwert+Crystal im selben Damage-Tick
 * - FacePlace: bei niedrigem HP MinDmg ignorieren
 * - Velocity-Prediction: Crystal dort platzieren wo Gegner hinläuft
 * - Async Positions-Berechnung (CompletableFuture, Thread-safe)
 * - Y-Achsen-Optimierung
 * - Anti-Suicide: nie wenn es uns tötet
 * - Debug-Modus
 *
 * ⚠️ Approximiert (nicht 100% server-präzise):
 * - Packet-Reihenfolge Hit+Pop beim D-Tap (1 Client-Tick Abstand)
 * - Prediction nur linear (keine Gravity/Friction-Modellierung)
 *
 * ❌ Nicht umsetzbar ohne Server-Zugriff:
 * - Blast-Protection in Schadensberechnung
 * - Bestätigung ob Crystal wirklich gespawnt ist vor Pop
 */
public class AutoCrystal extends HackModule {

    // === Settings ===
    private final NumberSetting placeSpeed   = addSetting(new NumberSetting("PlaceSpeed",   12, 1, 20, 1));
    private final NumberSetting breakSpeed   = addSetting(new NumberSetting("BreakSpeed",   20, 1, 20, 1));
    private final NumberSetting range        = addSetting(new NumberSetting("Range",        5.5, 2.0, 7.0, 0.5));
    private final NumberSetting wallRange    = addSetting(new NumberSetting("WallRange",    4.5, 1.0, 7.0, 0.5));
    private final NumberSetting minDamage    = addSetting(new NumberSetting("MinDmg",       2.0, 0.5, 20.0, 0.5));
    private final NumberSetting selfDmgLimit = addSetting(new NumberSetting("SelfDmgLimit", 8.0, 1.0, 20.0, 0.5));
    private final BoolSetting   instant      = addSetting(new BoolSetting("Instant",        true));
    private final BoolSetting   dtap         = addSetting(new BoolSetting("D-Tap",          true));
    private final BoolSetting   facePlace    = addSetting(new BoolSetting("FacePlace",      true));
    private final NumberSetting facePlaceHp  = addSetting(new NumberSetting("FacePlaceHP",  8.0, 1.0, 20.0, 0.5));
    private final BoolSetting   predict      = addSetting(new BoolSetting("Predict",        true));
    private final NumberSetting predictTicks = addSetting(new NumberSetting("PredTicks",    2, 1, 5, 1));
    private final BoolSetting   yAxis        = addSetting(new BoolSetting("Y-Axis",         true));
    private final BoolSetting   silentRot    = addSetting(new BoolSetting("SilentRot",      true));
    private final BoolSetting   packetPlace  = addSetting(new BoolSetting("PacketPlace",    true));
    private final BoolSetting   antiSelf     = addSetting(new BoolSetting("AntiSelf",       true));
    private final BoolSetting   debug        = addSetting(new BoolSetting("Debug",          false));

    // === Timing ===
    private long lastPlaceMs = 0;
    private long lastBreakMs = 0;
    private BlockPos lastPlacedPos = null;

    // === Async Calculation ===
    private final AtomicReference<PlaceResult> bestPlaceResult = new AtomicReference<>();
    private volatile boolean calcRunning = false;

    // === D-Tap State ===
    private enum DtapState { IDLE, HIT_SENT, DONE }
    private DtapState dtapState   = DtapState.IDLE;
    private long      dtapTimer   = 0;
    private EndCrystal dtapTarget = null;

    public AutoCrystal() {
        super("AutoCrystal", "Crystal PvP — Packet + D-Tap + Predict", ModuleCategory.COMBAT, GLFW.GLFW_KEY_C, false);
    }

    // === Tick ===

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        LocalPlayer self  = mc.player;
        long now          = System.currentTimeMillis();
        Player target     = findTarget(mc);
        if (target == null) return;

        int targetHurtTime = ((LivingEntityHurtAccessor) target).getHurtTime();
        boolean facePlaceActive = facePlace.getValue()
            && DamageUtil.getTotalHealth(target) <= facePlaceHp.getValue();

        // === Async Positions-Berechnung ===
        if (!calcRunning) {
            calcRunning = true;
            final Player tgt = target;
            CompletableFuture.runAsync(() -> {
                PlaceResult result = calcBestPlace(mc, tgt, self, facePlaceActive);
                bestPlaceResult.set(result);
                calcRunning = false;
            });
        }

        // === D-Tap ===
        if (dtap.getValue()) tickDtap(mc, self, target, now, targetHurtTime);

        // === Break ===
        if (dtapState == DtapState.IDLE) {
            long breakInterval = (long)(1000.0 / breakSpeed.getValue());
            if (now - lastBreakMs >= breakInterval) {
                if (breakBestCrystal(mc, target, self, facePlaceActive)) lastBreakMs = now;
            }
        }

        // === Place ===
        if (instant.getValue() && lastPlacedPos != null
                && !BlockUtil.crystalExistsAt(mc, lastPlacedPos.above())) {
            if (doPlace(mc, self, bestPlaceResult.get())) lastPlaceMs = now;
            return;
        }
        long placeInterval = (long)(1000.0 / placeSpeed.getValue());
        if (now - lastPlaceMs >= placeInterval) {
            if (doPlace(mc, self, bestPlaceResult.get())) lastPlaceMs = now;
        }
    }

    // === D-Tap ===

    /**
     * D-Tap Ablauf:
     * 1. IDLE: targetHurtTime == 0 → Schwert-Hit senden + D-Tap Crystal merken
     * 2. HIT_SENT: Crystal im gleichen/nächsten Tick per Paket attackieren
     * 3. DONE: warten bis Invincibility-Frames (10 Ticks / 500ms) vorbei
     *
     * ⚠️ Packet-Reihenfolge Hit+Pop ist Client-seitig approximiert.
     *    Server verarbeitet Pakete ggf. in getrennten Ticks.
     */
    private void tickDtap(Minecraft mc, LocalPlayer self, Player target, long now, int targetHurtTime) {
        switch (dtapState) {
            case IDLE -> {
                if (targetHurtTime > 0) return;
                EndCrystal crystal = findBestCrystalToPop(mc, target, self, 5.0);
                if (crystal == null) return;

                // Schwert-Hit — packet-basiert
                int swordSlot = findSwordSlot(self);
                if (swordSlot != -1) self.getInventory().setSelectedSlot(swordSlot);
                sendSilentAndAttack(mc, self, target.position(), () ->
                    mc.getConnection().send(
                        ServerboundInteractPacket.createAttackPacket(target, self.isShiftKeyDown())
                    )
                );
                self.swing(InteractionHand.MAIN_HAND);

                dtapState  = DtapState.HIT_SENT;
                dtapTimer  = now;
                dtapTarget = crystal;
                if (debug.getValue())
                    System.out.println("[AC-DTap] Hit sent on " + target.getName().getString());
            }
            case HIT_SENT -> {
                if (dtapTarget != null && dtapTarget.isAlive()) {
                    sendSilentAndAttack(mc, self, dtapTarget.position(), () ->
                        mc.getConnection().send(
                            ServerboundInteractPacket.createAttackPacket(dtapTarget, false)
                        )
                    );
                    self.swing(InteractionHand.MAIN_HAND);
                    if (debug.getValue())
                        System.out.println("[AC-DTap] Crystal popped");
                }
                dtapState  = DtapState.DONE;
                dtapTimer  = now;
                dtapTarget = null;
            }
            case DONE -> {
                if (targetHurtTime == 0 || now - dtapTimer > 600) dtapState = DtapState.IDLE;
            }
        }
    }

    // === Place ===

    private boolean doPlace(Minecraft mc, LocalPlayer self, PlaceResult result) {
        if (result == null) return false;
        BlockPos pos = result.pos;

        InteractionHand hand = getHandWithCrystal(self);
        if (hand == null) {
            int slot = findCrystalSlot(self);
            if (slot == -1) return false;
            self.getInventory().setSelectedSlot(slot);
            hand = InteractionHand.MAIN_HAND;
        }

        Vec3 placeVec = Vec3.atCenterOf(pos.above());

        if (packetPlace.getValue() && mc.getConnection() != null) {
            // Packet-basiertes Platzieren
            BlockHitResult hitResult = new BlockHitResult(placeVec, Direction.UP, pos, false);
            final InteractionHand finalHand = hand;
            sendSilentAndAttack(mc, self, placeVec, () ->
                mc.getConnection().send(new ServerboundUseItemOnPacket(
                    finalHand, hitResult, (int) mc.player.level().getGameTime()
                ))
            );
        } else {
            // Fallback: gameMode
            final InteractionHand finalHand = hand;
            sendSilentAndAttack(mc, self, placeVec, () ->
                mc.gameMode.useItemOn(self, finalHand,
                    new BlockHitResult(placeVec, Direction.UP, pos, false))
            );
        }

        lastPlacedPos = pos;
        if (debug.getValue())
            System.out.printf("[AC-Place] pos=%s dmg=%.1f%n", pos, result.damage);
        return true;
    }

    // === Break ===

    private boolean breakBestCrystal(Minecraft mc, Player target, LocalPlayer self, boolean faceActive) {
        double minDmg = faceActive ? 0.5 : minDamage.getValue() * 0.5;
        EndCrystal best = findBestCrystalToPop(mc, target, self, minDmg);
        if (best == null) return false;

        if (packetPlace.getValue() && mc.getConnection() != null) {
            final EndCrystal crystal = best;
            sendSilentAndAttack(mc, self, best.position(), () ->
                mc.getConnection().send(
                    ServerboundInteractPacket.createAttackPacket(crystal, self.isShiftKeyDown())
                )
            );
        } else {
            mc.gameMode.attack(self, best);
        }
        self.swing(InteractionHand.MAIN_HAND);

        if (debug.getValue())
            System.out.printf("[AC-Break] dmg=%.1f%n",
                DamageUtil.calcExplosionDamage(best.position(), target));
        return true;
    }

    // === Silent Rotation Helper ===

    private void sendSilentAndAttack(Minecraft mc, LocalPlayer self, Vec3 target, Runnable action) {
        if (silentRot.getValue() && mc.getConnection() != null) {
            Vec3 eyes = self.getEyePosition(1.0f);
            float[] rot = RotationUtil.calcRotation(eyes, target);
            RotationUtil.sendSilentRotation(mc, rot[0], rot[1]);
            action.run();
            RotationUtil.restoreRotation(mc);
        } else {
            action.run();
        }
    }

    // === Async Position Berechnung ===

    /**
     * Läuft im Hintergrund-Thread. Berechnet beste Crystal-Position.
     * Nutzt Velocity-Prediction wenn aktiviert.
     */
    private PlaceResult calcBestPlace(Minecraft mc, Player target, LocalPlayer self, boolean faceActive) {
        // Ziel-Position: aktuell oder predicted
        Vec3 targetPos = predict.getValue()
            ? PredictionUtil.predictPosition(target, predictTicks.getValue().intValue())
            : target.position();

        BlockPos tgtFeet = BlockPos.containing(targetPos);
        double effectiveMinDmg = faceActive ? 0.0 : minDamage.getValue();

        BlockPos bestPos   = null;
        double   bestScore = -1;
        double   bestDmg   = 0;

        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = tgtFeet.offset(x, y, z);

                    // Thread-safe level-Zugriff (read-only ist safe)
                    if (!BlockUtil.isValidCrystalBase(mc, pos)) continue;

                    Vec3 crystalPos = Vec3.atCenterOf(pos.above());
                    double distSelf = self.position().distanceTo(crystalPos);
                    boolean canSee  = canSeePos(mc, self, crystalPos);
                    if (distSelf > (canSee ? range.getValue() : wallRange.getValue())) continue;

                    // Schaden mit predicted Position berechnen
                    double dmgTarget = calcDmgAtPos(crystalPos, target, targetPos);
                    double dmgSelf   = DamageUtil.calcExplosionDamage(crystalPos, self);

                    if (dmgTarget < effectiveMinDmg) continue;
                    if (antiSelf.getValue() && dmgSelf >= self.getHealth()) continue;
                    if (antiSelf.getValue() && dmgSelf > selfDmgLimit.getValue()) continue;

                    // Y-Achsen-Bonus: Crystal auf Höhe der Füße = maximal Schaden
                    double yBonus = 0;
                    if (yAxis.getValue()) {
                        double yDiff = Math.abs((pos.getY() + 1) - targetPos.y);
                        yBonus = Math.max(0, 3.0 - yDiff * 1.5);
                    }

                    double score = dmgTarget + yBonus - dmgSelf * 0.3;
                    if (score > bestScore) {
                        bestScore = score;
                        bestPos   = pos;
                        bestDmg   = dmgTarget;
                    }
                }
            }
        }

        if (debug.getValue() && bestPos != null)
            System.out.printf("[AC-Calc] best=%s dmg=%.1f predicted=%s%n",
                bestPos, bestDmg, predict.getValue());

        return bestPos != null ? new PlaceResult(bestPos, bestDmg) : null;
    }

    /**
     * Schaden berechnen mit überschriebener Zielposition (für Prediction).
     */
    private double calcDmgAtPos(Vec3 crystalPos, Player target, Vec3 predictedPos) {
        Vec3 center = target.getBoundingBox().getCenter()
            .add(predictedPos.subtract(target.position()));
        double dist = crystalPos.distanceTo(center);
        if (dist > 12.0) return 0.0;

        double exposure = DamageUtil.calcExposure(crystalPos, target);
        double impact   = (1.0 - dist / 12.0) * exposure;
        double damage   = (impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0;

        double armor    = target.getArmorValue();
        double tough    = target.getAttributeValue(
            net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        double mit      = Math.min(20.0, Math.max(armor / 5.0, armor - damage / (2.0 + tough / 4.0)));
        return Math.max(0.0, damage * (1.0 - mit / 25.0));
    }

    // === Crystal-Suche ===

    private EndCrystal findBestCrystalToPop(Minecraft mc, Player target, LocalPlayer self, double minDmg) {
        List<EndCrystal> crystals = mc.level.getEntitiesOfClass(
            EndCrystal.class, self.getBoundingBox().inflate(range.getValue() + 1.0)
        );
        EndCrystal best  = null;
        double bestScore = 0;

        for (EndCrystal crystal : crystals) {
            if (!crystal.isAlive()) continue;
            double distSelf = self.distanceTo(crystal);
            boolean canSee  = canSeePos(mc, self, crystal.position());
            if (distSelf > (canSee ? range.getValue() : wallRange.getValue())) continue;

            double dmgTarget = DamageUtil.calcExplosionDamage(crystal.position(), target);
            double dmgSelf   = DamageUtil.calcExplosionDamage(crystal.position(), self);
            if (dmgTarget < minDmg) continue;
            if (antiSelf.getValue() && dmgSelf >= self.getHealth()) continue;
            if (antiSelf.getValue() && dmgSelf > selfDmgLimit.getValue()) continue;

            double score = dmgTarget - dmgSelf * 0.3;
            if (score > bestScore) { bestScore = score; best = crystal; }
        }
        return best;
    }

    // === Hilfsklasse ===

    private record PlaceResult(BlockPos pos, double damage) {}

    // === Utilities ===

    private boolean canSeePos(Minecraft mc, LocalPlayer self, Vec3 pos) {
        var result = mc.level.clip(new ClipContext(
            self.getEyePosition(1.0f), pos,
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, self
        ));
        return result.getType() == HitResult.Type.MISS;
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

    private int findSwordSlot(LocalPlayer p) {
        for (int i = 0; i < 9; i++)
            if (p.getInventory().getItem(i).is(ItemTags.SWORDS)) return i;
        return -1;
    }

    private Player findTarget(Minecraft mc) {
        Player nearest = null;
        double dist = 10.0;
        for (Player player : mc.level.players()) {
            if (player == mc.player || !player.isAlive()) continue;
            double d = mc.player.distanceTo(player);
            if (d < dist) { dist = d; nearest = player; }
        }
        return nearest;
    }
}
