package com.udhd.apiserver.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
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
}
