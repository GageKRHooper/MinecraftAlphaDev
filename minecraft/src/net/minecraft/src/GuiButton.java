package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiButton extends Gui {
    protected int width;
    protected int height;
    public int xPosition;
    public int yPosition;
    public String displayString;
    public int id;
    public boolean enabled;
    public boolean visible;

    // Parallax offsets for subtle mouse-based movement
    public float offsetX = 0;
    public float offsetY = 0;

    public GuiButton(int id, int x, int y, String text) {
        this(id, x, y, 200, 20, text);
    }

    protected GuiButton(int id, int x, int y, int width, int height, String text) {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = id;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
        this.displayString = text;
    }

    // Returns 0=disabled, 1=normal, 2=hover
    protected int getHoverState(boolean hovered) {
        if (!this.enabled) return 0;
        return hovered ? 2 : 1;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;

        FontRenderer font = mc.fontRenderer;

        // Determine if hovered
        boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                          mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

        // Smooth parallax offsets based on mouse
        float targetX = (mouseX - (this.xPosition + this.width / 2f)) * 0.02f;
        float targetY = (mouseY - (this.yPosition + this.height / 2f)) * 0.02f;
        offsetX += (targetX - offsetX) * 0.1f;
        offsetY += (targetY - offsetY) * 0.1f;

        // Scale up when hovered
        float scale = hovered ? 1.1f : 1.0f;

        GL11.glPushMatrix();
        GL11.glTranslatef(this.xPosition + this.width / 2f + offsetX, this.yPosition + this.height / 2f + offsetY, 0);
        GL11.glScalef(scale, scale, 1f);
        GL11.glTranslatef(-(this.xPosition + this.width / 2f), -(this.yPosition + this.height / 2f), 0);

        // Bind button texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture("/gui/gui.png"));
        GL11.glColor4f(1f, 1f, 1f, 1f);

        int hoverState = getHoverState(hovered);

        // Draw button halves
        drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + hoverState * 20, this.width / 2, this.height);
        drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + hoverState * 20, this.width / 2, this.height);

        // Draw button text
        int textColor;
        if (!this.enabled) textColor = 0xFF9D9D9D;
        else if (hovered) textColor = 0xFFFFE080;
        else textColor = 0xFFEEEEEE;

        drawCenteredString(font, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, textColor);

        GL11.glPopMatrix();
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        // Optional: add drag visuals
    }

    public void mouseReleased(int mouseX, int mouseY) {
        // Optional: add release effects
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.enabled && mouseX >= this.xPosition && mouseY >= this.yPosition &&
               mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }
}
