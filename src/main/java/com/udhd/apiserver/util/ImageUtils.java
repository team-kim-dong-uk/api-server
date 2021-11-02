package com.udhd.apiserver.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public class ImageUtils {

  public static BufferedImage load(String urlStr) throws IOException {
    URL url = new URL(urlStr);

    return ImageIO.read(url);
  }

  public static BufferedImage resize(BufferedImage inputImage, int width, int height) {
    BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());

    Graphics2D graphics2D = outputImage.createGraphics();
    graphics2D.drawImage(inputImage, 0, 0, width, height, null);
    graphics2D.dispose();

    return outputImage;
  }

  public static BufferedImage resizeAndGrayScale(BufferedImage inputImage, int width, int height) {
    BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

    Graphics2D graphics2D = outputImage.createGraphics();
    graphics2D.drawImage(inputImage, 0, 0, width, height, null);
    graphics2D.dispose();

    return outputImage;
  }

  public static BufferedImage createThumbnail(BufferedImage srcImage) {
    float thumbnailMinSize = 150;
    int srcHeight = srcImage.getHeight();
    int srcWidth = srcImage.getWidth();
    float scalingFactor;
    if (srcHeight >= srcWidth) {
      scalingFactor = thumbnailMinSize / srcWidth;
    } else {
      scalingFactor = thumbnailMinSize / srcHeight;
    }
    // Infer the scaling factor to avoid stretching the image
    // unnaturally
    int width = (int) (scalingFactor * srcWidth);
    int height = (int) (scalingFactor * srcHeight);
    BufferedImage resizedImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_RGB);
    Graphics2D g = resizedImage.createGraphics();
    // Fill with white before applying semi-transparent (alpha) images
    g.setPaint(Color.white);
    g.fillRect(0, 0, width, height);
    // Simple bilinear resize
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(srcImage, 0, 0, width, height, null);
    g.dispose();
    return resizedImage;
  }
}
