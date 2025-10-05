package com.aurionpro.app.service;

import java.io.IOException;

public interface QrCodeService {
    byte[] generateQrCodeImage(String payload) throws IOException;
}