package net.minecraft.src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiIngame extends Gui {
    private static RenderItem itemRenderer = new RenderItem();
    private List chatMessageList = new ArrayList();
    private Random rand = new Random();
    private Minecraft mc;
    private int updateCounter = 0;
    private String recordPlaying = "";
    private int recordPlayingUpFor = 0;

    // Required for PlayerControllerSP
    public float damageGuiPartialTime;
    float prevVignetteBrightness = 1.0F;

    public GuiIngame(Minecraft mc) {
        this.mc = mc;
    }

    public void renderGameOverlay(float partialTicks, boolean debugInfo, int width, int height) {
        ScaledResolution res = new ScaledResolution(mc.displayWidth, mc.displayHeight);
        int sw = res.getScaledWidth();
        int sh = res.getScaledHeight();
        FontRenderer font = mc.fontRenderer;
        mc.entityRenderer.setupOverlayRendering();

        GL11.glEnable(GL11.GL_BLEND);
        if (mc.options.fancyGraphics) {
            renderVignette(mc.thePlayer.getBrightness(partialTicks), sw, sh);
        }

        // Draw hotbar
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/gui.png"));
        InventoryPlayer inv = mc.thePlayer.inventory;
        this.zLevel = -90.0F;
        drawTexturedModalRect(sw / 2 - 91, sh - 22, 0, 0, 182, 22);
        drawTexturedModalRect(sw / 2 - 91 - 1 + inv.currentItem * 20, sh - 22 - 1, 0, 22, 24, 22);

        // Crosshair
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/icons.png"));
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
        drawTexturedModalRect(sw / 2 - 7, sh / 2 - 7, 0, 0, 16, 16);
        GL11.glDisable(GL11.GL_BLEND);

     // F3 Overlay
        if (Keyboard.isKeyDown(Keyboard.KEY_F3)) {
            // Title & Debug info
            font.drawStringWithShadow("AlphaOverhaul v0.0.1 (" + mc.debug + ")", 2, 2, 0xFFFFFF);
            font.drawStringWithShadow(mc.debugInfoRenders(), 2, 12, 0xFFFFFF);
            font.drawStringWithShadow(mc.getEntityDebug(), 2, 22, 0xFFFFFF);
            font.drawStringWithShadow(mc.debugInfoEntities(), 2, 32, 0xFFFFFF);

            // Memory usage
            long maxMem = Runtime.getRuntime().maxMemory();
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long used = totalMem - freeMem;
            String mem = String.format("Used memory: %d%% (%dMB) of %dMB", used * 100 / maxMem, used / 1024 / 1024, maxMem / 1024 / 1024);
            font.drawString(mem, sw - font.getStringWidth(mem) - 2, 2, 0xE0E0E0);
            String allocated = String.format("Allocated memory: %dMB", totalMem / 1024 / 1024);
            font.drawString(allocated, sw - font.getStringWidth(allocated) - 2, 12, 0xE0E0E0);

            // Player coordinates
            int px = MathHelper.floor_double(mc.thePlayer.posX);
            int py = MathHelper.floor_double(mc.thePlayer.posY);
            int pz = MathHelper.floor_double(mc.thePlayer.posZ);
            font.drawStringWithShadow("XYZ: " + px + ", " + py + ", " + pz, 2, 42, 0xFFFFFF);

            // Chunk coordinates
            int cx = px >> 4;
            int cz = pz >> 4;
            font.drawStringWithShadow("Chunk: " + cx + ", " + cz, 2, 52, 0xFFFFFF);

            // Facing (cardinal directions)
            float yaw = mc.thePlayer.rotationYaw % 360;
            if (yaw < 0) yaw += 360;
            String facing = "Unknown";
            if (yaw >= 315 || yaw < 45) facing = "South";
            else if (yaw >= 45 && yaw < 135) facing = "West";
            else if (yaw >= 135 && yaw < 225) facing = "North";
            else if (yaw >= 225 && yaw < 315) facing = "East";
            font.drawStringWithShadow("Facing: " + facing + " (" + MathHelper.floor_float(yaw) + "°)", 2, 62, 0xFFFFFF);

            // Block light and sky light
            int blockLight = mc.theWorld.getBlockLightValue(px, py, pz);   // light from torches, etc.
            float brightness = mc.theWorld.getBrightness(px, py, pz);  // combined light including sky
            font.drawStringWithShadow("Block Light: " + blockLight + " | Brightness: " + String.format("%.2f", brightness), 2, 72, 0xFFFFFF);

            // FPS
            font.drawStringWithShadow(mc.debug, 2, 82, 0xFFFFFF);
        } else {
            font.drawStringWithShadow("AlphaOverhaul v0.0.1 Client Test", 2, 2, 0xFFFFFF);
        }

        // Record playing
        if (recordPlayingUpFor > 0) {
            float fade = (float) recordPlayingUpFor - partialTicks;
            int alpha = (int) (fade * 256.0F / 20.0F);
            alpha = Math.min(255, alpha);
            if (alpha > 0) {
                GL11.glPushMatrix();
                GL11.glTranslatef(sw / 2, sh - 48, 0f);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                int color = Color.HSBtoRGB(fade / 50f, 0.7f, 0.6f) & 0xFFFFFF;
                font.drawString(recordPlaying, -font.getStringWidth(recordPlaying) / 2, -4, color + (alpha << 24));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }
        }

        // Chat messages
        byte maxChat = 10;
        boolean chatFocused = mc.currentScreen instanceof GuiChat;
        if (chatFocused) maxChat = 20;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, sh - 48, 0f);

        for (int i = 0; i < chatMessageList.size() && i < maxChat; i++) {
            ChatLine line = (ChatLine) chatMessageList.get(i);
            if (line.updateCounter < 200 || chatFocused) {
                double opacity = 1.0 - (double) line.updateCounter / 200.0;
                opacity = Math.max(0, Math.min(1, opacity)) * Math.max(0, Math.min(1, opacity));
                int alpha = (int) (255 * opacity);
                if (chatFocused) alpha = 255;
                if (alpha > 0) {
                    int y = -i * 9;
                    drawRect(2, y - 1, 2 + 320, y + 8, alpha / 2 << 24);
                    font.drawStringWithShadow(line.message, 2, y, 0xFFFFFF + (alpha << 24));
                }
            }
        }

        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);

        // Hotbar items
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glRotatef(180f, 1f, 0f, 0f);
        RenderHelper.enableStandardItemLighting();
        GL11.glPopMatrix();

        for (int i = 0; i < 9; i++) {
            int x = sw / 2 - 90 + i * 20 + 2;
            int y = sh - 16 - 3;
            renderInventorySlot(i, x, y, partialTicks);
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    private void renderVignette(float brightness, int width, int height) {
        brightness = 1f - brightness;
        brightness = Math.max(0f, Math.min(1f, brightness));
        this.prevVignetteBrightness += (brightness - this.prevVignetteBrightness) * 0.01f;

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
        GL11.glColor4f(this.prevVignetteBrightness, this.prevVignetteBrightness, this.prevVignetteBrightness, 1f);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/misc/vignette.png"));

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(0, height, -90, 0, 1);
        tess.addVertexWithUV(width, height, -90, 1, 1);
        tess.addVertexWithUV(width, 0, -90, 1, 0);
        tess.addVertexWithUV(0, 0, -90, 0, 0);
        tess.draw();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void renderInventorySlot(int i, int x, int y, float partialTicks) {
        ItemStack item = mc.thePlayer.inventory.mainInventory[i];
        if (item != null) {
            float anim = (float) item.animationsToGo - partialTicks;
            if (anim > 0f) {
                GL11.glPushMatrix();
                float scale = 1f + anim / 5f;
                GL11.glTranslatef(x + 8, y + 12, 0f);
                GL11.glScalef(1f / scale, (scale + 1f) / 2f, 1f);
                GL11.glTranslatef(-(x + 8), -(y + 12), 0f);
            }

            itemRenderer.renderItemIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);
            if (anim > 0f) GL11.glPopMatrix();
            itemRenderer.renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, item, x, y);
        }
    }

    public void updateTick() {
        if (recordPlayingUpFor > 0) recordPlayingUpFor--;
        updateCounter++;
        for (int i = 0; i < chatMessageList.size(); i++) {
            ((ChatLine) chatMessageList.get(i)).updateCounter++;
        }
    }

    public void addChatMessage(String msg) {
        while (mc.fontRenderer.getStringWidth(msg) > 320) {
            int split;
            for (split = 1; split < msg.length() && mc.fontRenderer.getStringWidth(msg.substring(0, split + 1)) <= 320; split++);
            addChatMessage(msg.substring(0, split));
            msg = msg.substring(split);
        }
        chatMessageList.add(0, new ChatLine(msg));
        while (chatMessageList.size() > 50) chatMessageList.remove(chatMessageList.size() - 1);
    }

    public void setRecordPlayingMessage(String msg) {
        this.recordPlaying = "Now playing: " + msg;
        this.recordPlayingUpFor = 60;
    }
}
