package com.ericjesse.conni.processors;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TrayIconUpdaterTest {

    Color transparent = new Color(0, 0, 0, 0);

    @Test
    public void listTransparentPixelsOfTransparent64Square() throws IOException {
        BufferedImage img = ImageIO.read(ClassLoader.getSystemResource("images/transparent-64x64.png"));
        int imageSize = 64;

        List<TrayIconUpdater.Pixel> expectedPixels = new ArrayList<>();
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                expectedPixels.add(new TrayIconUpdater.Pixel(x, y, 255));
            }
        }

        List<TrayIconUpdater.Pixel> pixelsToReplace = new TrayIconUpdater().listPixelsOfColor(img, transparent);
        for (int i = 0; i < expectedPixels.size(); i++) {
            assertEquals(expectedPixels.get(i), pixelsToReplace.get(i));
        }
    }

    @Test
    public void listTransparentPixelsOfTransparent128Square() throws IOException {
        BufferedImage img = ImageIO.read(ClassLoader.getSystemResource("images/transparent-128x128.png"));
        int imageSize = 128;

        List<TrayIconUpdater.Pixel> expectedPixels = new ArrayList<>();
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                expectedPixels.add(new TrayIconUpdater.Pixel(x, y, 255));
            }
        }

        List<TrayIconUpdater.Pixel> pixelsToReplace = new TrayIconUpdater().listPixelsOfColor(img, transparent);
        for (int i = 0; i < expectedPixels.size(); i++) {
            assertEquals(expectedPixels.get(i), pixelsToReplace.get(i));
        }
    }

    @Test
    public void listTransparentPixelsOfTransparentBorder64Square() throws IOException {
        BufferedImage img = ImageIO.read(ClassLoader.getSystemResource("images/transparent-border-64x64.png"));
        int imageSize = 64;

        List<TrayIconUpdater.Pixel> expectedPixels = new ArrayList<>();
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                // The center part 64x64 is not transparent.
                if (x < imageSize / 4 || x >= 3 * imageSize / 4 || y < imageSize / 4 || y >= 3 * imageSize / 4) {
                    expectedPixels.add(new TrayIconUpdater.Pixel(x, y, 255));
                }
            }
        }

        List<TrayIconUpdater.Pixel> pixelsToReplace = new TrayIconUpdater().listPixelsOfColor(img, transparent);
        for (int i = 0; i < expectedPixels.size(); i++) {
            assertEquals(expectedPixels.get(i), pixelsToReplace.get(i));
        }
    }

    @Test
    public void listTransparentPixelsOfTransparentBorder128Square() throws IOException {
        BufferedImage img = ImageIO.read(ClassLoader.getSystemResource("images/transparent-border-128x128.png"));
        int imageSize = 128;

        List<TrayIconUpdater.Pixel> expectedPixels = new ArrayList<>();
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                // The center part 64x64 is not transparent.
                if (x < imageSize / 4 || x >= 3 * imageSize / 4 || y < imageSize / 4 || y >= 3 * imageSize / 4) {
                    expectedPixels.add(new TrayIconUpdater.Pixel(x, y, 255));
                }
            }
        }

        List<TrayIconUpdater.Pixel> pixelsToReplace = new TrayIconUpdater().listPixelsOfColor(img, transparent);
        for (int i = 0; i < expectedPixels.size(); i++) {
            assertEquals(expectedPixels.get(i), pixelsToReplace.get(i));
        }
    }

    @Test
    public void convertWholeImageToYellow() throws IOException {
        BufferedImage img = ImageIO.read(ClassLoader.getSystemResource("images/transparent-64x64.png"));
        int imageSize = 64;

        List<TrayIconUpdater.Pixel> pixelsToReplace = new ArrayList<>();
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                pixelsToReplace.add(new TrayIconUpdater.Pixel(x, y, 255));
            }
        }

        new TrayIconUpdater().addBackgroundColor(img, pixelsToReplace, Color.YELLOW);
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                assertEquals(Color.YELLOW, new Color(img.getRGB(x, y)));
            }
        }
    }

    @Test
    public void convertThirdOfImageToYellow() throws IOException {
        BufferedImage img = ImageIO.read(ClassLoader.getSystemResource("images/transparent-64x64.png"));
        int imageSize = 64;

        List<TrayIconUpdater.Pixel> pixelsToReplace = new ArrayList<>();
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                if (x < imageSize / 3) { // Only the left third is set to yellow.
                    pixelsToReplace.add(new TrayIconUpdater.Pixel(x, y, 255));
                }
            }
        }

        new TrayIconUpdater().addBackgroundColor(img, pixelsToReplace, Color.YELLOW);
        //ImageIO.write(convertedImage, "png", new File("test-image.png"));
        for (int x = 0; x < imageSize; x++) {
            for (int y = 0; y < imageSize; y++) {
                Color color = new Color(img.getRGB(x, y), true);
                if (x < imageSize / 3) {
                    assertEquals(new TrayIconUpdater.Pixel(x, y, 255) + " should be yellow", Color.YELLOW, color);
                } else {
                    assertEquals(new TrayIconUpdater.Pixel(x, y, 255) + " should be transparent, but is " + color, 0,
                            color.getAlpha());
                }
            }
        }
    }

}
