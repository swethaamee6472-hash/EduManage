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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

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
     */
    public static String readQRCode(BufferedImage image) throws Exception {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
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
