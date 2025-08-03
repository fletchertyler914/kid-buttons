package fletchertyler914.kidbuttons;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KidButtons implements ModInitializer {
	public static final String MOD_ID = "kid-buttons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// The client-side functionality is handled in KidButtonsClient
		LOGGER.info("Initializing Kid-Friendly Buttons mod");
		LOGGER.info("Client-side functionality will be registered in the client initializer");
	}
}
