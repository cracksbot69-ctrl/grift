package com.cracksbot.modules;

import com.cracksbot.mixin.LivingEntityHurtAccessor;
import com.cracksbot.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * PearlFlash — Wirft automatisch eine Ender Pearl wenn der Spieler Schaden nimmt.
 *
 * ✅ Funktioniert:
 * - Erkennt Beginn eines Damage-Ticks (hurtTime: 0 → > 0)
 * - Wirft Pearl im selben Tick → negiert Knockback (Anti-KB durch Pearl-Velocity)
 * - Cooldown verhindert Spam
 * - Sucht Pearl in Hotbar + Offhand
 *
 * ⚠️ Eingeschränkt:
 * - Pearl wird in Blickrichtung geworfen — Ziel muss manuell anvisiert werden
 *   oder RotSpoof nutzen. Die Pearl-Richtung ist deine aktuelle Look-Direction.
 * - Server-seitige Knockback-Unterdrückung: funktioniert nur wenn Pearl-
 *   Impuls größer ist als der Knockback-Impuls (meist der Fall bei Crystals).
 *
 * ❌ Nicht implementierbar:
 * - Automatisch in optimale Pearl-Richtung rotieren ohne Velocity-Vorhersage
 */
public class AutoPearl extends HackModule {

    private final NumberSetting cooldown = addSetting(new NumberSetting("Cooldown", 500, 100, 2000, 50));

    private int  prevHurtTime = 0;
    private long lastPearlMs  = 0;

    public AutoPearl() {
        super("PearlFlash", "Pearl bei Schaden (Anti-KB)", ModuleCategory.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        LocalPlayer self = mc.player;
        int hurtTime = ((LivingEntityHurtAccessor) self).getHurtTime();

        // Erkennt Beginn eines Damage-Ticks
        boolean justHurt = prevHurtTime == 0 && hurtTime > 0;
        prevHurtTime = hurtTime;

        if (!justHurt) return;

        long now = System.currentTimeMillis();
        if (now - lastPearlMs < cooldown.getValue()) return;

        // Pearl suchen (Offhand bevorzugt da kein Slot-Wechsel nötig)
        InteractionHand hand = null;
        if (self.getOffhandItem().is(Items.ENDER_PEARL)) {
            hand = InteractionHand.OFF_HAND;
        } else {
            for (int i = 0; i < 9; i++) {
                if (self.getInventory().getItem(i).is(Items.ENDER_PEARL)) {
                    self.getInventory().setSelectedSlot(i);
                    hand = InteractionHand.MAIN_HAND;
                    break;
                }
            }
        }

        if (hand == null) return;

        mc.gameMode.useItem(self, hand);
        lastPearlMs = now;
    }
}
