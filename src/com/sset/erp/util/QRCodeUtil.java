package com.sset.erp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class QRCodeUtil {

    /**
     * Generates a QR code image from the given text.
     */
    public static BufferedImage generateQRCode(String text, int width, int height) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Reads and decodes a QR code from a BufferedImage.
     * Uses multi-pass decoding: normal, center-cropped, inverted (for phone screens),
     * and binarizer fallbacks.
     */
    public static String readQRCode(BufferedImage image) throws Exception {
        if (image == null) return null;

        // 1. Direct decode attempt
        String result = decodeSingleImage(image);
        if (result != null) return result;

        // 2. Center crop (phone held in center of camera frame - avoids peripheral glare/noise)
        int w = image.getWidth();
        int h = image.getHeight();
        if (w > 120 && h > 120) {
            int cropW = (int) (w * 0.65);
            int cropH = (int) (h * 0.65);
            int cropX = (w - cropW) / 2;
            int cropY = (h - cropH) / 2;
            BufferedImage cropped = image.getSubimage(cropX, cropY, cropW, cropH);
            result = decodeSingleImage(cropped);
            if (result != null) return result;
        }

        // 3. Inverted colors (fixes phone screen glare / dark mode / washed out backlights)
        BufferedImage inverted = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = inverted.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        byte[] pixels = ((java.awt.image.DataBufferByte) inverted.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = (byte) (255 - (pixels[i] & 0xFF));
        }
        result = decodeSingleImage(inverted);
        if (result != null) return result;

        return null;
    }

    private static String decodeSingleImage(BufferedImage image) {
        if (image == null) return null;

        java.util.Map<com.google.zxing.DecodeHintType, Object> hints = new java.util.EnumMap<>(com.google.zxing.DecodeHintType.class);
        hints.put(com.google.zxing.DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(com.google.zxing.DecodeHintType.POSSIBLE_FORMATS, java.util.Collections.singletonList(BarcodeFormat.QR_CODE));

        MultiFormatReader reader = new MultiFormatReader();
        reader.setHints(hints);

        LuminanceSource source = new BufferedImageLuminanceSource(image);

        // Try HybridBinarizer first
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result res = reader.decodeWithState(bitmap);
            if (res != null) return res.getText();
        } catch (Exception ignored) {
        } finally {
            reader.reset();
        }

        // Fallback: GlobalHistogramBinarizer (superior for backlit screens and uneven lighting)
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new com.google.zxing.common.GlobalHistogramBinarizer(source));
            Result res = reader.decodeWithState(bitmap);
            if (res != null) return res.getText();
        } catch (Exception ignored) {
        }

        return null;
    }
    
    /**
     * Reads and decodes a QR code from an image file.
     */
    public static String readQRCode(File file) throws Exception {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new Exception("Could not read image file.");
        }
        return readQRCode(image);
    }
}
