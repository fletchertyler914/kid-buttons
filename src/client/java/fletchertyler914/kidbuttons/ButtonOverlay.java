package fletchertyler914.kidbuttons;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ButtonOverlay {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    
    // Layout configuration for full-screen interface
    private static final int BUTTON_SIZE = 80; // Larger buttons for kids
    private static final int BUTTON_MARGIN = 24; // More spacing
    private static final int GRID_COLS = 4; // 4 columns for grid
    private static final int GRID_ROWS = 3; // 3 rows for all actions
    private static final int HEADER_HEIGHT = 0; // No header
    // Beautiful color scheme for kids
    private static final int BG_GRADIENT_TOP = 0xE6432371; // Deep purple
    private static final int BG_GRADIENT_BOTTOM = 0xE6614385; // Lighter purple
    private static final int BUTTON_NORMAL = 0xF0FFFFFF; // Bright white
    private static final int BUTTON_HOVER = 0xFFFFE066; // Bright yellow
    private static final int BUTTON_SHADOW = 0x80000000; // Subtle shadow
    private static final int BORDER_COLOR = 0xFF9966CC; // Purple border
    private static final int BUTTON_BORDER_HOVER = 0xFF66FF66; // Green hover border
    private static final int TITLE_COLOR = 0xFFFFD700; // Gold title
    private static final int CATEGORY_COLOR = 0xFFFFFFFF; // White category labels
    private static final int TOOLTIP_BG = 0xF0432371; // Purple tooltip
    private static final int TOOLTIP_TEXT = 0xFFFFFFFF; // White tooltip text
    private static final List<ButtonData> BUTTONS = new ArrayList<>();
    private static ButtonData hoveredButton = null;
    private static long lastClickTime = 0;
    private static final long CLICK_COOLDOWN = 300;
    private static boolean wasMousePressed = false;
    private static boolean shouldShowInterface = false;
    private static boolean buttonsInitialized = false;
    
    // Home coordinate storage
    private static int homeX = 0;
    private static int homeY = 64;
    private static int homeZ = 0;
    private static boolean homeSet = false;
    
    public static void register() {
        HudRenderCallback.EVENT.register(ButtonOverlay::render);
    }
    
    public static boolean isOverlayVisible() {
        return shouldShowInterface;
    }
    
    public static void setHomeCoordinates(int x, int y, int z) {
        homeX = x;
        homeY = y;
        homeZ = z;
        homeSet = true;
        KidButtons.LOGGER.info("Kid Buttons: Home coordinates set to ({}, {}, {})", x, y, z);
    }
    
    public static String getHomeCommand() {
        if (homeSet) {
            return "tp @p " + homeX + " " + homeY + " " + homeZ;
        } else {
            // If no home is set, try to get the world spawn point
            if (client.world != null) {
                var spawnPos = client.world.getSpawnPos();
                if (spawnPos != null) {
                    return "tp @p " + spawnPos.getX() + " " + spawnPos.getY() + " " + spawnPos.getZ();
                }
            }
            // Fallback to a safe location away from world spawn
            return "tp @p 100 64 100"; // Safe distance from world spawn
        }
    }
    
    private static void initializeButtons() {
        if (buttonsInitialized || client.getWindow() == null) {
            return;
        }
        BUTTONS.clear();
        // Game Modes
        BUTTONS.add(new ButtonData(new ItemStack(Items.WOODEN_SWORD), "gamemode survival @p", "Survival Mode"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.DIRT), "gamemode creative @p", "Creative Mode"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.FILLED_MAP), "gamemode adventure @p", "Adventure Mode"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.BARRIER), "gamemode spectator @p", "Spectator Mode"));

        // Time - Simple color-coded blocks for instant recognition
        BUTTONS.add(new ButtonData(new ItemStack(Items.YELLOW_CONCRETE), "time set 0", "Sunrise (6 AM)"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.YELLOW_STAINED_GLASS), "time set 6000", "Noon"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.ORANGE_CONCRETE), "time set 12000", "Sunset (6 PM)"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.BLACK_CONCRETE), "time set 18000", "Midnight"));
        // Weather - Glass blocks for sky/weather clarity
        BUTTONS.add(new ButtonData(new ItemStack(Items.WHITE_STAINED_GLASS), "weather clear", "Clear Sky"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.BLUE_STAINED_GLASS), "weather rain", "Rain"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.GRAY_STAINED_GLASS), "weather thunder", "Thunder Storm"));
        // Extra Kid-Friendly Commands
        BUTTONS.add(new ButtonData(new ItemStack(Items.RED_BED), "SET_HOME", "Set Home Point"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.ENDER_PEARL), "GO_HOME", "Teleport Home"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.GOLDEN_APPLE), "effect give @p minecraft:instant_health 1 10", "Heal"));
        BUTTONS.add(new ButtonData(new ItemStack(Items.COOKED_BEEF), "effect give @p minecraft:saturation 30 1", "Feed"));
        buttonsInitialized = true;
    }
    
    private static void render(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (client.player == null || client.world == null) {
            return;
        }
        shouldShowInterface = client.currentScreen instanceof ChatScreen;
        if (!shouldShowInterface) {
            return;
        }
        initializeButtons();
        int mouseX = (int) (client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth());
        int mouseY = (int) (client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight());
        hoveredButton = null;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        // --- Improved layout with perfect spacing and symmetry ---
        int[] rowCounts = {4, 4, 3, BUTTONS.size() - 11}; // game, time, weather, extras
        String[] categoryNames = {"🎮 GAME MODES", "⏰ TIME", "🌤️ WEATHER", "✨ EXTRAS"};
        int numRows = rowCounts.length;
        int maxRowCount = 0;
        for (int c : rowCounts) maxRowCount = Math.max(maxRowCount, c);
        
        // Improved spacing constants for better visual balance (much smaller)
        int buttonSize = 20; // Much smaller buttons
        int buttonMargin = 8; // Tighter spacing
        int categorySpacing = 12; // Reduced vertical separation
        int titlePadding = 12; // Less space around title
        int categoryLabelPadding = 4; // Minimal space between label and buttons
        int sidePadding = 24; // Reduced horizontal padding
        int minButtonSize = 16;
        int minButtonMargin = 4;
        
        // Calculate optimal button size to fit screen width
        int neededWidth = maxRowCount * buttonSize + (maxRowCount - 1) * buttonMargin + sidePadding * 2;
        while (neededWidth > screenWidth - 80 && buttonSize > minButtonSize) {
            buttonSize -= 3;
            buttonMargin = Math.max(minButtonMargin, buttonMargin - 1);
            neededWidth = maxRowCount * buttonSize + (maxRowCount - 1) * buttonMargin + sidePadding * 2;
        }
        
        // Calculate overlay dimensions with improved proportions
        int overlayWidth = neededWidth;
        int overlayHeight = titlePadding * 2 + numRows * buttonSize + (numRows - 1) * categorySpacing + categoryLabelPadding * numRows + 40; // Add extra height to cover EXTRAS section
        int overlayLeft = (screenWidth - overlayWidth) / 2;
        int overlayTop = Math.max(10, (screenHeight - overlayHeight) / 2 - 50); // Start much higher up to cover title and icons
        
        // Draw beautiful gradient background with rounded corners
        drawGradientRoundedRect(drawContext, overlayLeft, overlayTop, overlayWidth, overlayHeight, 12, BG_GRADIENT_TOP, BG_GRADIENT_BOTTOM);
        // Add subtle border glow
        drawRoundedBorder(drawContext, overlayLeft - 2, overlayTop - 2, overlayWidth + 4, overlayHeight + 4, 14, 0x80FFD700);
        
        // Draw stunning title with improved positioning
        int titleY = overlayTop + titlePadding;
        drawContext.drawCenteredTextWithShadow(client.textRenderer, Text.literal("🎮 KID BUTTONS 🎮").formatted(Formatting.BOLD), screenWidth / 2, titleY, TITLE_COLOR);
        
        // Draw each category row with perfect alignment
        int y = overlayTop + titlePadding * 2 + categoryLabelPadding;
        int btnIdx = 0;
        for (int row = 0; row < numRows; row++) {
            // Draw category label with better positioning
            drawContext.drawCenteredTextWithShadow(client.textRenderer, Text.literal(categoryNames[row]).formatted(Formatting.BOLD), screenWidth / 2, y, CATEGORY_COLOR);
            y += categoryLabelPadding + 4; // Small gap between label and buttons
            
            int count = rowCounts[row];
            int rowWidth = count * buttonSize + (count - 1) * buttonMargin;
            int startX = overlayLeft + (overlayWidth - rowWidth) / 2;
            
            for (int i = 0; i < count; i++) {
                int x = startX + i * (buttonSize + buttonMargin);
                drawBeautifulButton(drawContext, BUTTONS.get(btnIdx++), x, y, buttonSize, mouseX, mouseY);
            }
            y += buttonSize + categorySpacing;
        }
        // Draw beautiful tooltip with improved positioning
        if (hoveredButton != null && hoveredButton.tooltip != null) {
            int tooltipPadding = 12;
            int tooltipX = mouseX + 16;
            int tooltipY = mouseY - 30;
            int tooltipWidth = client.textRenderer.getWidth(hoveredButton.tooltip) + tooltipPadding * 2;
            int tooltipHeight = 24;
            
            // Keep tooltip on screen with better margins
            if (tooltipX + tooltipWidth > screenWidth - 20) {
                tooltipX = mouseX - tooltipWidth - 16;
            }
            if (tooltipY < 20) {
                tooltipY = mouseY + 30;
            }
            
            // Draw tooltip background with rounded corners
            drawGradientRoundedRect(drawContext, tooltipX, tooltipY, tooltipWidth, tooltipHeight, 6, TOOLTIP_BG, TOOLTIP_BG);
            
            // Draw tooltip border
            drawRoundedBorder(drawContext, tooltipX - 1, tooltipY - 1, tooltipWidth + 2, tooltipHeight + 2, 7, 0x80FFFFFF);
            
            // Draw tooltip text with better centering
            int textX = tooltipX + tooltipPadding;
            int textY = tooltipY + (tooltipHeight - client.textRenderer.fontHeight) / 2;
            drawContext.drawTextWithShadow(client.textRenderer, Text.literal(hoveredButton.tooltip), textX, textY, TOOLTIP_TEXT);
        }
    }
    
    private static void updateDynamicButtons() {
        // This method is no longer needed as buttons are static
    }
    
    public static void handleClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null || !shouldShowInterface) {
            return;
        }
        boolean isMousePressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (isMousePressed && !wasMousePressed) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime >= CLICK_COOLDOWN) {
                if (hoveredButton != null) {
                    executeButtonAction(hoveredButton);
                    lastClickTime = currentTime;
                    if (client.currentScreen != null) {
                        client.currentScreen.close();
                    }
                }
            }
        }
        wasMousePressed = isMousePressed;
    }
    
    private static void executeButtonAction(ButtonData buttonData) {
        if (client.player == null || client.player.networkHandler == null) {
            return;
        }
        String command = buttonData.command;
        
        // Handle special commands
        if ("SET_HOME".equals(command)) {
            // Get current player position
            int x = (int) Math.floor(client.player.getX());
            int y = (int) Math.floor(client.player.getY());
            int z = (int) Math.floor(client.player.getZ());
            
            // Store home coordinates
            setHomeCoordinates(x, y, z);
            
            // Set spawn point in game at current location
            String spawnCommand = "spawnpoint @p " + x + " " + y + " " + z;
            client.player.networkHandler.sendChatCommand(spawnCommand);
            KidButtons.LOGGER.info("Kid Buttons: Set home and spawn point to current location: {}, {}, {}", x, y, z);
            return;
        }
        
        if ("GO_HOME".equals(command)) {
            String homeCommand = getHomeCommand();
            client.player.networkHandler.sendChatCommand(homeCommand);
            KidButtons.LOGGER.info("Kid Buttons: Teleporting home with command: {}", homeCommand);
            return;
        }
        
        // Handle regular commands
        client.player.networkHandler.sendChatCommand(command);
        KidButtons.LOGGER.info("Kid Buttons: Executed command: {}", command);
    }
    
    private static void drawButton(DrawContext drawContext, ButtonData buttonData, int x, int y, int size, int mouseX, int mouseY) {
        boolean isHovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        if (isHovered) hoveredButton = buttonData;
        int bgColor = isHovered ? BUTTON_HOVER : BUTTON_NORMAL;
        int borderCol = isHovered ? BUTTON_BORDER_HOVER : BORDER_COLOR;
        drawContext.fill(x, y, x + size, y + size, bgColor);
        drawContext.drawBorder(x, y, size, size, borderCol);
        // Draw item icon centered
        ItemStack iconStack = buttonData.icon;
        int iconX = x + size / 2 - 8;
        int iconY = y + size / 2 - 8;
        drawContext.drawItem(iconStack, iconX, iconY);
    }
    
    private static void drawGradientRoundedRect(DrawContext drawContext, int x, int y, int width, int height, int radius, int colorTop, int colorBottom) {
        // Simple gradient approximation with multiple fills
        for (int i = 0; i < height; i++) {
            float ratio = (float) i / height;
            int color = interpolateColor(colorTop, colorBottom, ratio);
            drawContext.fill(x + (i < radius ? radius - (int)Math.sqrt(radius * radius - (radius - i) * (radius - i)) : 0), 
                           y + i, 
                           x + width - (i < radius ? radius - (int)Math.sqrt(radius * radius - (radius - i) * (radius - i)) : 0), 
                           y + i + 1, 
                           color);
        }
    }
    
    private static void drawRoundedBorder(DrawContext drawContext, int x, int y, int width, int height, int radius, int color) {
        // Simple border drawing
        drawContext.drawBorder(x, y, width, height, color);
    }
    
    private static int interpolateColor(int color1, int color2, float ratio) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * ratio);
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    private static void drawBeautifulButton(DrawContext drawContext, ButtonData buttonData, int x, int y, int size, int mouseX, int mouseY) {
        boolean isHovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
        if (isHovered) hoveredButton = buttonData;
        
        // Draw button shadow with improved positioning
        int shadowOffset = 2;
        drawContext.fill(x + shadowOffset, y + shadowOffset, x + size + shadowOffset, y + size + shadowOffset, BUTTON_SHADOW);
        
        // Draw beautiful button background with rounded corners
        int bgColor = isHovered ? BUTTON_HOVER : BUTTON_NORMAL;
        int borderColor = isHovered ? BUTTON_BORDER_HOVER : BORDER_COLOR;
        int borderWidth = isHovered ? 2 : 1;
        
        // Draw main button background
        drawContext.fill(x, y, x + size, y + size, bgColor);
        
        // Draw border with improved thickness
        for (int i = 0; i < borderWidth; i++) {
            drawContext.drawBorder(x - i, y - i, size + 2 * i, size + 2 * i, borderColor);
        }
        
        // Draw item icon perfectly centered with improved positioning
        ItemStack iconStack = buttonData.icon;
        int iconSize = 16; // Standard Minecraft item size
        int iconX = x + (size - iconSize) / 2;
        int iconY = y + (size - iconSize) / 2;
        
        // Add subtle hover animation
        if (isHovered) {
            iconY -= 1; // Slight lift effect
        }
        
        drawContext.drawItem(iconStack, iconX, iconY);
    }
    
    // Basic button data
    private static class ButtonData {
        ItemStack icon;
        String command;
        String tooltip;
        ButtonData(ItemStack icon, String command, String tooltip) {
            this.icon = icon;
            this.command = command;
            this.tooltip = tooltip;
        }
    }
} 
