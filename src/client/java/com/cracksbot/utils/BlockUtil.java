package com.cracksbot.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public final class BlockUtil {

    private BlockUtil() {}

    /**
     * Prüft ob auf diesem Block ein Crystal platziert werden kann:
     * - Obsidian oder Bedrock als Basis
     * - 2 Blöcke Luft darüber
     * - Kein Crystal oder Spieler in der Zielposition
     */
    public static boolean isValidCrystalBase(Minecraft mc, BlockPos pos) {
        var base = mc.level.getBlockState(pos);
        if (!base.is(Blocks.OBSIDIAN) && !base.is(Blocks.BEDROCK)) return false;
        if (!mc.level.getBlockState(pos.above()).isAir()) return false;
        if (!mc.level.getBlockState(pos.above(2)).isAir()) return false;

        // Nur Crystals und Spieler blockieren die Platzierung
        AABB checkBox = new AABB(pos.above());
        return mc.level.getEntities(
            (Entity) null, checkBox,
            e -> e instanceof EndCrystal || (e instanceof Player && e != mc.player)
        ).isEmpty();
    }

    /**
     * Gibt zurück ob an einer Position bereits ein Crystal existiert.
     */
    public static boolean crystalExistsAt(Minecraft mc, BlockPos above) {
        AABB box = new AABB(above).inflate(0.5);
        return !mc.level.getEntitiesOfClass(EndCrystal.class, box).isEmpty();
    }
}
