package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GuiTexturePacks extends GuiScreen {
    private GuiScreen parentScreen;
    private List<PackInfo> texturePackList = new ArrayList<>();
    private int selectedIndex = 0;
    private int scrollOffset = 0;

    private static final int BUTTON_HEIGHT = 20;
    private static final int VISIBLE_BUTTONS = 10;
    private static final int LIST_WIDTH = 160;
    private static final int PREVIEW_SIZE = 64;

    private float previewOffsetX = 0f;
    private float previewOffsetY = 0f;

    public GuiTexturePacks(GuiScreen parent) {
        this.parentScreen = parent;
        loadTexturePackMetadata();
    }

    private static class PackInfo {
        String name;
        File file;
        String description;
        int textureID = -1;

        PackInfo(String name, File file) {
            this.name = name;
            this.file = file;
        }
    }

    private void debug(String msg) {
        System.out.println("[GuiTexturePacks DEBUG] " + msg);
    }

    private void loadTexturePackMetadata() {
        File dir = new File(System.getProperty("user.dir") + "/texturepacks");
        debug("Loading texture packs from: " + dir.getAbsolutePath());

        // Step 1: Add Default pack
        File defaultDir = new File(dir, "Default");
        PackInfo defaultPack = new PackInfo("Default", defaultDir);
        defaultPack.description = "The original look of Minecraft.";
        texturePackList.add(defaultPack);
        selectedIndex = 0;

        // Step 2: Scan the texturepacks folder for other packs
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory() || f.getName().endsWith(".zip")) {
                    if (f.getName().equalsIgnoreCase("Default")) continue;

                    PackInfo pack = new PackInfo(f.getName(), f);
                    loadPackDescription(pack);
                    texturePackList.add(pack);
                }
            }
        }
    }

    private void loadPackDescription(PackInfo pack) {
        try {
            pack.description = "No description available.";
            if (pack.file == null) return;

            if (pack.file.isDirectory()) {
                File descFile = new File(pack.file, "pack/pack.txt");
                if (descFile.exists()) pack.description = readFile(descFile);
            } else if (pack.file.getName().endsWith(".zip")) {
                try (ZipFile zip = new ZipFile(pack.file)) {
                    ZipEntry descEntry = zip.getEntry("pack/pack.txt");
                    if (descEntry != null) pack.description = readZipEntry(zip, descEntry);
                }
            }
        } catch (Exception e) {
            pack.description = "Error loading pack.";
            e.printStackTrace();
        }
    }

    private String readFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        br.close();
        return sb.toString().trim();
    }

    private String readZipEntry(ZipFile zip, ZipEntry entry) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        br.close();
        return sb.toString().trim();
    }

    @Override
    public void initGui() {
        this.controlList.clear();
        updateButtons();

        // Add Apply/Done buttons
        this.controlList.add(new GuiButton(300, this.width / 2 + 50, this.height - 60, 80, 20, "Apply"));
        this.controlList.add(new GuiButton(200, this.width / 2 + 50, this.height - 30, 80, 20, "Done"));

        // Load preview textures for all packs
        for (PackInfo pack : texturePackList) {
            if (pack.textureID == -1) {
                BufferedImage img = null;

                try {
                    if (pack.file != null && pack.file.isDirectory()) {
                        File iconFile = new File(pack.file, "pack/pack.png");
                        if (iconFile.exists()) {
                            img = ImageIO.read(iconFile);
                        }
                    } else if (pack.file != null && pack.file.getName().endsWith(".zip")) {
                        try (ZipFile zip = new ZipFile(pack.file)) {
                            ZipEntry iconEntry = zip.getEntry("pack/pack.png");
                            if (iconEntry != null) img = ImageIO.read(zip.getInputStream(iconEntry));
                        }
                    }

                    // Fallback if no pack.png found
                    if (img == null) {
                        img = new BufferedImage(PREVIEW_SIZE, PREVIEW_SIZE, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = img.createGraphics();
                        g.setColor(Color.LIGHT_GRAY);
                        g.fillRect(0, 0, PREVIEW_SIZE, PREVIEW_SIZE);
                        g.setColor(Color.DARK_GRAY);
                        g.drawRect(0, 0, PREVIEW_SIZE - 1, PREVIEW_SIZE - 1);
                        g.dispose();
                    }

                    if (img != null) {
                        // Crop to square
                        int size = Math.min(img.getWidth(), img.getHeight());
                        int x = (img.getWidth() - size) / 2;
                        int y = (img.getHeight() - size) / 2;
                        BufferedImage squareImg = img.getSubimage(x, y, size, size);

                        // Resize to PREVIEW_SIZE
                        BufferedImage scaled = new BufferedImage(PREVIEW_SIZE, PREVIEW_SIZE, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = scaled.createGraphics();
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g.setComposite(AlphaComposite.Src);
                        g.drawImage(squareImg, 0, 0, PREVIEW_SIZE, PREVIEW_SIZE, null);
                        g.dispose();

                        pack.textureID = mc.renderEngine.allocateAndSetupTexture(scaled);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Always load Default pack in Minecraft
        if (selectedIndex == 0) {
            textureManager.restoreDefault(mc);
        }
    }

    private void updateButtons() {
        controlList.removeIf(obj -> obj instanceof GuiButton && ((GuiButton) obj).id >= 500);

        int y = this.height / 6;
        int x = 20;
        for (int i = scrollOffset; i < texturePackList.size() && i < scrollOffset + VISIBLE_BUTTONS; i++) {
            PackInfo pack = texturePackList.get(i);
            GuiButton button = new GuiButton(500 + i, x, y, LIST_WIDTH, BUTTON_HEIGHT, pack.name);
            if (i == selectedIndex) button.displayString = "> " + pack.name;
            controlList.add(button);
            y += BUTTON_HEIGHT + 4;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 200) mc.displayGuiScreen(parentScreen);
        else if (button.id == 300 && selectedIndex >= 0) {
            PackInfo pack = texturePackList.get(selectedIndex);
            if (pack.file == null) textureManager.restoreDefault(mc);
            else textureManager.loadTexturePack(mc, pack.name);
        }

        int index = button.id - 500;
        if (index >= 0 && index < texturePackList.size()) {
            selectedIndex = index;
            updateButtons();
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            scrollOffset -= Integer.signum(dWheel);
            scrollOffset = Math.max(0, Math.min(texturePackList.size() - VISIBLE_BUTTONS, scrollOffset));
            updateButtons();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "Texture Packs", width / 2, 10, 0xFFFFFF);

        if (selectedIndex >= 0 && selectedIndex < texturePackList.size()) {
            PackInfo pack = texturePackList.get(selectedIndex);

            int baseX = width / 2 + 50;
            int baseY = height / 2 - PREVIEW_SIZE;

            float targetX = (mouseX - (baseX + PREVIEW_SIZE / 2)) * 0.02f;
            float targetY = (mouseY - (baseY + PREVIEW_SIZE / 2)) * 0.02f;
            previewOffsetX += (targetX - previewOffsetX) * 0.1f;
            previewOffsetY += (targetY - previewOffsetY) * 0.1f;
            int offsetX = (int) previewOffsetX;
            int offsetY = (int) previewOffsetY;

            if (pack.textureID != -1) {
                drawFullTexture(baseX + offsetX, baseY + offsetY, PREVIEW_SIZE, pack.textureID);
            } else {
                drawRect(baseX + offsetX, baseY + offsetY, baseX + offsetX + PREVIEW_SIZE, baseY + offsetY + PREVIEW_SIZE, 0xFF333333);
            }

            int centerX = baseX + PREVIEW_SIZE / 2 + offsetX;
            int titleY = baseY + PREVIEW_SIZE + 5 + offsetY;
            drawCenteredString(fontRenderer, pack.name, centerX, titleY, 0xFFFFFF);

            List<String> lines = wrapText(pack.description, 150);
            int lineHeight = fontRenderer.FONT_HEIGHT;
            int descStartY = titleY + lineHeight;
            for (int i = 0; i < lines.size(); i++) {
                drawCenteredString(fontRenderer, lines.get(i), centerX, descStartY + i * lineHeight, 0xAAAAAA);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawFullTexture(int x, int y, int size, int textureID) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glColor4f(1f, 1f, 1f, 1f); // <-- fixed (4 args)

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0f, 0f); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0f, 1f); GL11.glVertex2f(x, y + size);
        GL11.glTexCoord2f(1f, 1f); GL11.glVertex2f(x + size, y + size);
        GL11.glTexCoord2f(1f, 0f); GL11.glVertex2f(x + size, y);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;

        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String w : words) {
            String test = current.length() == 0 ? w : current + " " + w;
            if (fontRenderer.getStringWidth(test) <= maxWidth)
                current.append(current.length() == 0 ? w : " " + w);
            else {
                if (current.length() > 0) lines.add(current.toString());
                current = new StringBuilder(w);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }
}
