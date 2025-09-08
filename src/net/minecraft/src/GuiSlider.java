package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class GuiSlider extends GuiButton {
    public float sliderValue = 1.0F;
    public boolean dragging = false;
    private int idFloat = 0;

    private final int KNOB_SIZE = 16;
    public float offsetX = 0;
    public float offsetY = 0;

    public GuiSlider(int id, int x, int y, int idFloat, String displayString, float value) {
        super(id, x, y, 150, 20, displayString);
        this.idFloat = idFloat;
        this.sliderValue = value;
    }

    @Override
    protected int getHoverState(boolean mouseOver) {
        return 0; // no hover effect for track
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            updateSliderValue(mc, mouseX);
            this.dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible && this.dragging) {
            updateSliderValue(mc, mouseX);
        }

        if (this.visible) {
            // Base knob position relative to the button/track
            int knobX = this.xPosition + (int) (this.sliderValue * (this.width - KNOB_SIZE));
            int knobY = this.yPosition + (this.height - KNOB_SIZE) / 2;

            // Small visual adjustment to center the knob better
            int shiftX = 0;
            int shiftY = 0;

            // Parallax offsets
            float targetX = (mouseX - (this.xPosition + this.width / 2f)) * 0.02f;
            float targetY = (mouseY - (this.yPosition + this.height / 2f)) * 0.02f;
            offsetX += (targetX - offsetX) * 0.1f;
            offsetY += (targetY - offsetY) * 0.1f;

            GL11.glPushMatrix();
            GL11.glTranslatef(offsetX, offsetY, 0);

            mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/terrain.png"));
            GL11.glColor4f(1F, 1F, 1F, 1F);

            // Draw knob with shift applied
            this.drawTexturedModalRect(knobX + shiftX, knobY + shiftY, (6*16), 0, KNOB_SIZE, KNOB_SIZE);

            GL11.glPopMatrix();
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            super.drawButton(mc, mouseX, mouseY); // draw the track first
            this.mouseDragged(mc, mouseX, mouseY); // draw the knob on top
        }
    }

    private void updateSliderValue(Minecraft mc, int mouseX) {
        this.sliderValue = (float) (mouseX - this.xPosition) / (float) (this.width - KNOB_SIZE);
        if (this.sliderValue < 0F) this.sliderValue = 0F;
        if (this.sliderValue > 1F) this.sliderValue = 1F;

        mc.options.setOptionFloatValue(this.idFloat, this.sliderValue);
        this.displayString = mc.options.getOptionDisplayString(this.idFloat);
    }
}
