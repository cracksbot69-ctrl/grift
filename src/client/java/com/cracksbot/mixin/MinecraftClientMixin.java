package com.cracksbot.mixin;

import com.cracksbot.CracksBotClient;
import com.cracksbot.modules.Criticals;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        if (CracksBotClient.INSTANCE == null) return;
        Criticals crits = CracksBotClient.INSTANCE.get(Criticals.class);
        if (crits != null && crits.isEnabled()) {
            crits.onAttack(Minecraft.getInstance());
        }
    }
}
