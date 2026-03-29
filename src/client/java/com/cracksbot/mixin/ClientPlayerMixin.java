package com.cracksbot.mixin;

import com.cracksbot.CracksBotClient;
import com.cracksbot.modules.Velocity;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class ClientPlayerMixin {

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void onKnockback(double strength, double x, double z, CallbackInfo ci) {
        if (CracksBotClient.INSTANCE == null) return;
        Velocity velo = CracksBotClient.INSTANCE.get(Velocity.class);
        if (velo != null && velo.isEnabled()) {
            // Cancel knockback entirely or reduce it
            ci.cancel();
        }
    }
}
