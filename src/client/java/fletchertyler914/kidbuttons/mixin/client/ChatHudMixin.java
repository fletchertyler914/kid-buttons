package fletchertyler914.kidbuttons.mixin.client;

import fletchertyler914.kidbuttons.ButtonOverlay;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        // Hide the chat HUD when the kid buttons overlay is visible
        if (ButtonOverlay.isOverlayVisible()) {
            ci.cancel();
        }
    }
} 
