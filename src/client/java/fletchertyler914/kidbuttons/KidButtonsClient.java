package fletchertyler914.kidbuttons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class KidButtonsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register the button overlay for HUD rendering
        ButtonOverlay.register();
        
        // Register client tick event to handle mouse clicks
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle mouse clicks during client tick
            ButtonOverlay.handleClientTick(client);
        });
        
        KidButtons.LOGGER.info("Kid-Friendly Buttons client initialized!");
    }
}
