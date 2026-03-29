package com.cracksbot.mixin;

import com.cracksbot.CracksBotClient;
import com.cracksbot.modules.Velocity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true)
    private void onPush(double x, double y, double z, CallbackInfo ci) {
        if (CracksBotClient.INSTANCE == null) return;
        Velocity velo = CracksBotClient.INSTANCE.get(Velocity.class);
        if (velo != null && velo.isEnabled()) {
            ci.cancel(); // Block entity pushing
        }
    }
}
