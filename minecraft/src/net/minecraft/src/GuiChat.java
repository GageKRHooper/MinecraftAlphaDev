package net.minecraft.src;

import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;

public class GuiChat extends GuiScreen {
    private String message = "";
    private int updateCounter = 0;

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.width = this.mc.displayWidth;
        this.height = this.mc.displayHeight;
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    public void updateScreen() {
        ++this.updateCounter;
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(null);
        } else if (keyCode == 28) { // Enter
            String trimmed = this.message.trim();
            if (trimmed.length() > 0) {
                this.mc.thePlayer.sendChatMessage(trimmed);
            }
            this.mc.displayGuiScreen(null);
        } else {
            if (keyCode == 14 && this.message.length() > 0) { // Backspace
                this.message = this.message.substring(0, this.message.length() - 1);
            }

            String allowedChars = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_abcdefghijklmnopqrstuvwxyz{|}~\u2302\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb";
            if (allowedChars.indexOf(typedChar) >= 0 && this.message.length() < 100) {
                this.message += typedChar;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw chat input box
        this.drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);

        // Draw current message with blinking cursor
        String displayText = "> " + this.message + ((this.updateCounter / 6 % 2 == 0) ? "_" : "");
        this.drawString(this.fontRenderer, displayText, 4, this.height - 12, 14737632);
    }
    
}
