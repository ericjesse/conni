package com.ericjesse.conni.processors;

import com.ericjesse.conni.http.HttpResponse;
import com.ericjesse.conni.http.errors.ConniError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;

/**
 * TrayIconUpdater is an observer to update the tray icon and reflect the connection status.
 */
public class TrayIconUpdater implements ResponseObserver {

    public static final String TRAY_TOOLTIP = "Conni\n\nConnection is %s";

    public static final Color COLOR_TO_REPLACE = Color.BLACK;

    public static final String STATUS_UNKNOWN = "unknown";

    public static final String STATUS_DOWN = "down";

    public static final String STATUS_UP = "up (%d ms)";

    private static final Logger LOG = LoggerFactory.getLogger(TrayIconUpdater.class);

    private static final String IMAGE_PATH = "images/028-connection.png";

    private static final Color ERROR_COLOR = new Color(255, 53, 62);

    private static final Color OK_COLOR = new Color(102, 255, 79);

    private static final Color UNKOWN_COLOR = new Color(64, 107, 255);

    private final URL imageUrl;

    private final List<Pixel> transparentPixels;

    private final Semaphore trayIconRefreshSemaphore = new Semaphore(1, true);

    private final SystemTray systemTray;

    private final TrayIcon trayIcon;

    private BufferedImage bufferedImage;

    private boolean isSystemTraySupported;

    public TrayIconUpdater() throws IOException {
        isSystemTraySupported = SystemTray.isSupported();

        if (isSystemTraySupported) {
            imageUrl = ClassLoader.getSystemResource(IMAGE_PATH);
            // The image should be found.
            assert null != imageUrl;
            bufferedImage = ImageIO.read(imageUrl);
            transparentPixels = listPixelsOfColor(bufferedImage, COLOR_TO_REPLACE);

            // Add the tray icon to the bar.
            systemTray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(bufferedImage, String.format(TRAY_TOOLTIP, STATUS_UNKNOWN));
            trayIcon.setImageAutoSize(true);
            try {

                final PopupMenu popup = new PopupMenu();
                CheckboxMenuItem pauseCb = new CheckboxMenuItem("Pause");
                MenuItem quitItem = new MenuItem("Quit");

                popup.add(pauseCb);
                popup.addSeparator();
                popup.add(quitItem);

                // Quit the program.
                quitItem.addActionListener(event -> System.exit(0));

                trayIcon.setPopupMenu(popup);
                systemTray.add(trayIcon);
                updateTrayIconWithColorAndRefresBufferedImage(UNKOWN_COLOR);
            } catch (AWTException e) {
                // If the tray icon cannot be added, no need to process it later.
                isSystemTraySupported = false;
                LOG.error(e.getMessage(), e);
            }
        } else {
            imageUrl = null;
            transparentPixels = null;
            systemTray = null;
            trayIcon = null;
        }
    }

    /**
     * List all the transparent pixels of an image.
     *
     * @param image the image to parse.
     * @return the list of all the transparent pixels.
     */
    // Visible for tests.
    List<Pixel> listPixelsOfColor(final BufferedImage image, final Color colorToRetrieve) {
        final ArrayList<Pixel> pixels = new ArrayList<>();

        final int width = image.getWidth();
        final int height = image.getHeight();
        Color pixelColor;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelColor = new Color(image.getRGB(x, y), true);
                if (pixelColor.getRed() == colorToRetrieve.getRed() && pixelColor.getGreen() == colorToRetrieve
                        .getGreen() && pixelColor.getBlue() == colorToRetrieve.getBlue()) {
                    pixels.add(new Pixel(x, y, pixelColor.getAlpha()));
                }
            }
        }
        return pixels;

    }

    @Override
    public ConniError processError(final ConniError error) {
        if (isSystemTraySupported) {
            updateTrayIconWithColorAndRefresBufferedImage(ERROR_COLOR);
            try {
                trayIconRefreshSemaphore.acquire();
                trayIcon.setToolTip(String.format(TRAY_TOOLTIP, STATUS_DOWN));
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
                // Clean up state.
                Thread.currentThread().interrupt();
            } finally {
                trayIconRefreshSemaphore.release();
            }
        }
        return error;
    }

    @Override
    public HttpResponse processResponse(final HttpResponse response) {
        if (isSystemTraySupported) {
            updateTrayIconWithColorAndRefresBufferedImage(OK_COLOR);
            try {
                trayIconRefreshSemaphore.acquire();
                trayIcon.setToolTip(
                        String.format(TRAY_TOOLTIP, String.format(STATUS_UP, response.getDuration().toMillis())));
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
                // Clean up state.
                Thread.currentThread().interrupt();
            } finally {
                trayIconRefreshSemaphore.release();
            }
        }
        return response;
    }

    /**
     * Update the transparent background of the bufferedImage to the expected color.
     *
     * @param backgroundColor the color to set for the background.
     */
    private void updateTrayIconWithColorAndRefresBufferedImage(final Color backgroundColor) {
        // Use the already prepared buffered image.
        final BufferedImage img = deepCopy(this.bufferedImage);

        addBackgroundColor(img, transparentPixels, backgroundColor);
        try {
            trayIconRefreshSemaphore.acquire();
            trayIcon.setImage(img);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            // Clean up state.
            Thread.currentThread().interrupt();
        } finally {
            trayIconRefreshSemaphore.release();
        }
    }

    private BufferedImage deepCopy(final BufferedImage bi) {
        final ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        final WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    @Override
    public int getOrder() {
        return 1;
    }

    // Visible for tests.
    void addBackgroundColor(final BufferedImage image, final List<Pixel> transparentPixels,
            final Color backgroundColor) {
        transparentPixels.parallelStream().forEach(p -> image.setRGB(p.x, p.y,
                new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), p.alpha)
                        .getRGB()));
    }

    // Visible for test.
    static class Pixel {

        final int x;

        final int y;

        final int alpha;

        public Pixel(final int x, final int y, final int alpha) {
            this.x = x;
            this.y = y;
            this.alpha = alpha;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Pixel pixel = (Pixel) o;
            return x == pixel.x && y == pixel.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Pixel{");
            sb.append("x=").append(x);
            sb.append(", y=").append(y);
            sb.append('}');
            return sb.toString();
        }
    }
}
