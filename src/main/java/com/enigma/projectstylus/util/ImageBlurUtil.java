package com.enigma.projectstylus.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ImageBlurUtil {

    public static byte[] blurImageBytes(byte[] originalImageBytes, int radius) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
        if (originalImage == null) {
            throw new IllegalArgumentException("Unable to decode image bytes");
        }

        // Convert to standard RGB to prevent color space artifacts during JPEG write
        BufferedImage rgbImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        rgbImage.createGraphics().drawImage(originalImage, 0, 0, null);

        // Separable horizontal and vertical pass
        BufferedImage blurred = boxBlur(rgbImage, radius);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(blurred, "jpg", baos);
        return baos.toByteArray();
    }

    private static BufferedImage boxBlur(BufferedImage src, int radius) {
        int size = radius * 2 + 1;
        float weight = 1.0f / size;
        float[] hData = new float[size];
        Arrays.fill(hData, weight);

        // Horizontal blur
        Kernel hKernel = new Kernel(size, 1, hData);
        ConvolveOp hOp = new ConvolveOp(hKernel, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage hImg = hOp.filter(src, null);

        // Vertical blur
        Kernel vKernel = new Kernel(1, size, hData);
        ConvolveOp vOp = new ConvolveOp(vKernel, ConvolveOp.EDGE_NO_OP, null);
        return vOp.filter(hImg, null);
    }

    public static int getBlurRadiusForOrder(int order) {
        return switch (order) {
            case 1 -> 22;
            case 2 -> 16;
            case 3 -> 12;
            case 4 -> 9;
            case 5 -> 7;
            default -> 7;
        };
    }
}