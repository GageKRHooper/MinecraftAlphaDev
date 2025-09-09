package net.minecraft.src;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class textureManager {

    // ../texturepacks relative to working directory
    private static final File textureDir = new File(System.getProperty("user.dir") + "/texturepacks");

    /**
     * Main entry point — load a texture pack by name (zip or folder).
     */
    public static void loadTexturePack(Minecraft mc, String packName) {
        File pack = new File(textureDir, packName);
        if (!pack.exists()) {
            System.out.println("Texture pack not found: " + pack.getAbsolutePath());
            return;
        }

        if (pack.isDirectory()) {
            loadFolderPack(mc, pack);
        } else if (packName.toLowerCase().endsWith(".zip")) {
            loadZipPack(mc, pack);
        } else {
            System.out.println("Unknown texture pack format: " + pack.getAbsolutePath());
        }
    }

    /**
     * Load a folder-based texture pack.
     */
    private static void loadFolderPack(Minecraft mc, File folder) {
        System.out.println("Loading folder texture pack: " + folder.getName());
        LoadingScreenRenderer loadingScreen = new LoadingScreenRenderer(mc);

        try {
            loadingScreen.resetProgressAndMessage("Loading texture pack...");
            // Recursively replace all PNGs in the folder
            loadFolderRecursive(mc, loadingScreen, folder, folder.getAbsolutePath());
            loadingScreen.displayProgressMessage("Done loading textures!");
            System.out.println("Texture pack loaded successfully: " + folder.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadFolderRecursive(Minecraft mc, LoadingScreenRenderer screen, File folder, String basePath) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                loadFolderRecursive(mc, screen, file, basePath);
            } else if (file.getName().toLowerCase().endsWith(".png")) {
                String relativePath = file.getAbsolutePath().substring(basePath.length()).replace("\\", "/");
                if (!relativePath.startsWith("/")) relativePath = "/" + relativePath;
                tryReplaceFromFile(mc, screen, file, relativePath);
            }
        }
    }

    private static void tryReplaceFromFile(Minecraft mc, LoadingScreenRenderer screen, File file, String target) {
        if (!file.exists()) return;
        try {
            BufferedImage img = ImageIO.read(file);
            replaceTexture(mc, screen, img, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load a zip-based texture pack.
     */
    private static void loadZipPack(Minecraft mc, File zip) {
        System.out.println("Loading zip texture pack: " + zip.getName());
        LoadingScreenRenderer loadingScreen = new LoadingScreenRenderer(mc);

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            loadingScreen.resetProgressAndMessage("Loading texture pack...");

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = "/" + entry.getName().replace("\\", "/"); // normalize path
                if (name.toLowerCase().endsWith(".png")) {
                    BufferedImage img = ImageIO.read(zis);
                    replaceTexture(mc, loadingScreen, img, name);
                }
                zis.closeEntry();
            }

            loadingScreen.displayProgressMessage("Done loading textures!");
            System.out.println("Texture pack loaded successfully: " + zip.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Restore default textures by reloading them from the JAR.
     */
    public static void restoreDefault(Minecraft mc) {
        System.out.println("Restoring default textures...");
        mc.renderEngine.refreshTextures();
    }

    // ----------------- Helpers -----------------

    private static void replaceTexture(Minecraft mc, LoadingScreenRenderer screen, BufferedImage img, String target) {
        try {
            int texID = mc.renderEngine.getTexture(target); // Ensure the texture is registered
            mc.renderEngine.setupTexture(img, texID);       // Upload new pixels
            System.out.println("Replaced " + target);
            screen.displayLoadingString("Loaded " + target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
