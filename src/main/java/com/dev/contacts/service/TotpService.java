package com.dev.contacts.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class TotpService {
  private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

  public GoogleAuthenticatorKey createCredentials() {
    return gAuth.createCredentials();
  }

  public String getOtpAuthURL(String issuer, String account, GoogleAuthenticatorKey key) {
    return GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, account, key);
  }

  public boolean verifyCode(String secret, int code) {
    return gAuth.authorize(secret, code);
  }

  public byte[] generateQrPng(String otpAuthUrl, int width, int height) throws WriterException, IOException {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, width, height);
    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
    return pngOutputStream.toByteArray();
  }
}
