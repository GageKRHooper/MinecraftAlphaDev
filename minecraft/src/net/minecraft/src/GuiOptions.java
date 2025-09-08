package net.minecraft.src;

public class GuiOptions extends GuiScreen {
    private GuiScreen parentScreen;
    protected String screenTitle = "Options";
    private GameSettings options;

    // Reference to the texture packs button
    private GuiButton texturePacksButton;

    public GuiOptions(GuiScreen var1, GameSettings var2) {
        this.parentScreen = var1;
        this.options = var2;
    }

    public void initGui() {
        for (int var1 = 0; var1 < this.options.numberOfOptions; ++var1) {
            int var2 = this.options.isSlider(var1);
            if (var2 == 0) {
                this.controlList.add(new GuiSmallButton(var1,
                    this.width / 2 - 155 + var1 % 2 * 160,
                    this.height / 6 + 24 * (var1 >> 1),
                    this.options.getOptionDisplayString(var1)));
            } else {
                this.controlList.add(new GuiSlider(var1,
                    this.width / 2 - 155 + var1 % 2 * 160,
                    this.height / 6 + 24 * (var1 >> 1),
                    var1,
                    this.options.getOptionDisplayString(var1),
                    this.options.getOptionFloatValue(var1)));
            }
        }

        // Controls button
        this.controlList.add(new GuiButton(100, this.width / 2 - 100,
            this.height / 6 + 115 + 5, "Controls..."));

        // === New Texture Packs button (visible but disabled) ===
        this.texturePacksButton = new GuiButton(300, this.width / 2 - 100,
            this.height / 6 + 145, "Texture Packs (WIP)...");
        this.texturePacksButton.enabled = false; // Disable click
        this.controlList.add(this.texturePacksButton);

        // Done button
        this.controlList.add(new GuiButton(200, this.width / 2 - 100,
            this.height / 6 + 175, "Done"));
    }

    protected void actionPerformed(GuiButton var1) {
        if (var1.enabled) { // This automatically ignores disabled buttons
            if (var1.id < 100) {
                this.options.setOptionValue(var1.id, 1);
                var1.displayString = this.options.getOptionDisplayString(var1.id);
            }

            if (var1.id == 100) {
                this.mc.displayGuiScreen(new GuiControls(this, this.options));
            }

            if (var1.id == 200) {
                this.mc.displayGuiScreen(this.parentScreen);
            }

            if (var1.id == 300) {
                // Won't trigger because button is disabled
            }
        }
    }

    public void drawScreen(int var1, int var2, float var3) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, this.screenTitle, this.width / 2, 20, 16777215);
        super.drawScreen(var1, var2, var3);
    }
}
