package net.minecraft.src;

import java.io.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GameSettings {
    private static final String[] RENDER_DISTANCES = new String[]{"FAR", "NORMAL", "SHORT", "TINY"};
    private static final String[] DIFFICULTY_LEVELS = new String[]{"Peaceful", "Easy", "Normal", "Hard"};

    public float musicVolume = 1.0F;
    public float soundVolume = 1.0F;
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse = false;
    public int renderDistance = 0;
    public boolean viewBobbing = true;
    public boolean anaglyph = false;
    public boolean limitFramerate = false;
    public boolean fancyGraphics = true;

    public KeyBinding keyBindForward = new KeyBinding("Forward", 17);
    public KeyBinding keyBindLeft = new KeyBinding("Left", 30);
    public KeyBinding keyBindBack = new KeyBinding("Back", 31);
    public KeyBinding keyBindRight = new KeyBinding("Right", 32);
    public KeyBinding keyBindJump = new KeyBinding("Jump", 57);
    public KeyBinding keyBindInventory = new KeyBinding("Inventory", 23);
    public KeyBinding keyBindDrop = new KeyBinding("Drop", 16);
    public KeyBinding keyBindChat = new KeyBinding("Chat", 20);
    public KeyBinding keyBindToggleFog = new KeyBinding("Toggle fog", 33);
    public KeyBinding keyBindSneak = new KeyBinding("Sneak", 42);

    public KeyBinding[] keyBindings = new KeyBinding[]{
        this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight,
        this.keyBindJump, this.keyBindSneak, this.keyBindDrop, this.keyBindInventory,
        this.keyBindChat, this.keyBindToggleFog
    };

    protected Minecraft mc;
    private File optionsFile;

    public int numberOfOptions = 10;
    public int difficulty = 2;
    public boolean thirdPersonView = false;

    // New field for selected texture pack
    public String texturePack = "";

    public GameSettings(Minecraft mc, File mcDataDir) {
        this.mc = mc;
        this.optionsFile = new File(mcDataDir, "options.txt");
        this.loadOptions();
    }

    public String getKeyBindingDescription(int index) {
        return this.keyBindings[index].keyDescription + ": " + Keyboard.getKeyName(this.keyBindings[index].keyCode);
    }

    public void setKeyBinding(int index, int keyCode) {
        this.keyBindings[index].keyCode = keyCode;
        this.saveOptions();
    }

    public void setOptionFloatValue(int id, float value) {
        switch (id) {
            case 0 -> { this.musicVolume = value; this.mc.sndManager.onSoundOptionsChanged(); }
            case 1 -> { this.soundVolume = value; this.mc.sndManager.onSoundOptionsChanged(); }
            case 3 -> this.mouseSensitivity = value;
        }
    }

    public void setOptionValue(int id, int increment) {
        switch (id) {
            case 2 -> this.invertMouse = !this.invertMouse;
            case 4 -> this.renderDistance = (this.renderDistance + increment) & 3;
            case 5 -> this.viewBobbing = !this.viewBobbing;
            case 6 -> { this.anaglyph = !this.anaglyph; this.mc.renderEngine.refreshTextures(); }
            case 7 -> this.limitFramerate = !this.limitFramerate;
            case 8 -> this.difficulty = (this.difficulty + increment) & 3;
            case 9 -> { this.fancyGraphics = !this.fancyGraphics; this.mc.renderGlobal.loadRenderers(); }
        }
        this.saveOptions();
    }

    public int isSlider(int id) {
        return (id == 0 || id == 1 || id == 3) ? 1 : 0;
    }

    public float getOptionFloatValue(int id) {
        return switch (id) {
            case 0 -> this.musicVolume;
            case 1 -> this.soundVolume;
            case 3 -> this.mouseSensitivity;
            default -> 0.0F;
        };
    }

    public String getOptionDisplayString(int id) {
        return switch (id) {
            case 0 -> "Music: " + (this.musicVolume > 0 ? (int)(this.musicVolume * 100) + "%" : "OFF");
            case 1 -> "Sound: " + (this.soundVolume > 0 ? (int)(this.soundVolume * 100) + "%" : "OFF");
            case 2 -> "Invert mouse: " + (this.invertMouse ? "ON" : "OFF");
            case 3 -> "Sensitivity: " + (int)(this.mouseSensitivity * 200) + "%";
            case 4 -> "Render distance: " + RENDER_DISTANCES[this.renderDistance];
            case 5 -> "View bobbing: " + (this.viewBobbing ? "ON" : "OFF");
            case 6 -> "3D anaglyph: " + (this.anaglyph ? "ON" : "OFF");
            case 7 -> "Limit framerate: " + (this.limitFramerate ? "ON" : "OFF");
            case 8 -> "Difficulty: " + DIFFICULTY_LEVELS[this.difficulty];
            case 9 -> "Graphics: " + (this.fancyGraphics ? "FANCY" : "FAST");
            default -> "";
        };
    }

    public void loadOptions() {
        try {
            if (!this.optionsFile.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(this.optionsFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                switch (parts[0]) {
                    case "music" -> this.musicVolume = parseFloat(parts[1]);
                    case "sound" -> this.soundVolume = parseFloat(parts[1]);
                    case "mouseSensitivity" -> this.mouseSensitivity = parseFloat(parts[1]);
                    case "invertYMouse" -> this.invertMouse = parts[1].equals("true");
                    case "viewDistance" -> this.renderDistance = Integer.parseInt(parts[1]);
                    case "bobView" -> this.viewBobbing = parts[1].equals("true");
                    case "anaglyph3d" -> this.anaglyph = parts[1].equals("true");
                    case "limitFramerate" -> this.limitFramerate = parts[1].equals("true");
                    case "difficulty" -> this.difficulty = Integer.parseInt(parts[1]);
                    case "fancyGraphics" -> this.fancyGraphics = parts[1].equals("true");
                    case "texturePack" -> this.texturePack = parts[1];
                    default -> {
                        for (int i = 0; i < this.keyBindings.length; i++) {
                            if (parts[0].equals("key_" + this.keyBindings[i].keyDescription)) {
                                this.keyBindings[i].keyCode = Integer.parseInt(parts[1]);
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Failed to load options");
            e.printStackTrace();
        }
    }

    private float parseFloat(String s) {
        return switch (s) {
            case "true" -> 1.0F;
            case "false" -> 0.0F;
            default -> Float.parseFloat(s);
        };
    }

    public void saveOptions() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(this.optionsFile));
            writer.println("music:" + this.musicVolume);
            writer.println("sound:" + this.soundVolume);
            writer.println("invertYMouse:" + this.invertMouse);
            writer.println("mouseSensitivity:" + this.mouseSensitivity);
            writer.println("viewDistance:" + this.renderDistance);
            writer.println("bobView:" + this.viewBobbing);
            writer.println("anaglyph3d:" + this.anaglyph);
            writer.println("limitFramerate:" + this.limitFramerate);
            writer.println("difficulty:" + this.difficulty);
            writer.println("fancyGraphics:" + this.fancyGraphics);
            writer.println("texturePack:" + (this.texturePack != null ? this.texturePack : ""));

            for (KeyBinding kb : this.keyBindings) {
                writer.println("key_" + kb.keyDescription + ":" + kb.keyCode);
            }

            writer.close();
        } catch (Exception e) {
            System.out.println("Failed to save options");
            e.printStackTrace();
        }
    }

    public String getSelectedTexturePack() {
        return this.texturePack;
    }

    public void setSelectedTexturePack(String packName) {
        this.texturePack = packName;
        this.saveOptions();
    }
}
