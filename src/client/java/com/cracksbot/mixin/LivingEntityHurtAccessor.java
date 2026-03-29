package com.cracksbot.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityHurtAccessor {
    @Accessor("hurtTime")
    int getHurtTime();

    @Accessor("lastHurt")
    float getLastHurt();
}
