package com.aurionpro.app.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class QrCodeServiceImpl implements QrCodeService {

    @Override
    public byte[] generateQrCodeImage(String payload) throws IOException {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("QR code payload cannot be empty.");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            
            log.info("Successfully generated QR code image.");
            return pngOutputStream.toByteArray();

        } catch (WriterException e) {
            log.error("Could not generate QR code", e);
            // Re-throw as a more standard IO exception
            throw new IOException("Could not generate QR code image", e);
        }
    }
}