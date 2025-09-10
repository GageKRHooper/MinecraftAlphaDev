package net.minecraft.src;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GuiOptions extends GuiScreen {
    private GuiScreen parentScreen;
    protected String screenTitle = "Options";
    private GameSettings options;

    // Reference to the texture packs button
    private GuiButton texturePacksButton;

    public GuiOptions(GuiScreen parent, GameSettings options) {
        this.parentScreen = parent;
        this.options = options;
    }

    public void initGui() {
        this.controlList.clear();

        for (int i = 0; i < this.options.numberOfOptions; ++i) {
            int isSlider = this.options.isSlider(i);
            if (isSlider == 0) {
                this.controlList.add(new GuiSmallButton(i,
                        this.width / 2 - 155 + i % 2 * 160,
                        this.height / 6 + 24 * (i >> 1),
                        this.options.getOptionDisplayString(i)));
            } else {
                this.controlList.add(new GuiSlider(i,
                        this.width / 2 - 155 + i % 2 * 160,
                        this.height / 6 + 24 * (i >> 1),
                        i,
                        this.options.getOptionDisplayString(i),
                        this.options.getOptionFloatValue(i)));
            }
        }

        // Controls button
        this.controlList.add(new GuiButton(100, this.width / 2 - 100,
                this.height / 6 + 115 + 5, "Controls..."));

        // === Texture Packs button (ENABLED) ===
        this.texturePacksButton = new GuiButton(300, this.width / 2 - 100,
                this.height / 6 + 145, "Texture Packs...");
        this.controlList.add(this.texturePacksButton);

        // Done button
        this.controlList.add(new GuiButton(200, this.width / 2 - 100,
                this.height / 6 + 175, "Done"));
    }

    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (button.id < 100) {
            this.options.setOptionValue(button.id, 1);
            button.displayString = this.options.getOptionDisplayString(button.id);
        }

        if (button.id == 100) {
            this.mc.displayGuiScreen(new GuiControls(this, this.options));
        }

        if (button.id == 200) {
            this.mc.displayGuiScreen(this.parentScreen);
        }

        if (button.id == 300) {
            this.mc.displayGuiScreen(new GuiTexturePacks(this));
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
