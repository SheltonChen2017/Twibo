package edu.miis.Service;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

@Service
public class CaptchaService {

    public static final String SESSION_KEY = "TWIBO_CAPTCHA";
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final SecureRandom random = new SecureRandom();

    public String createCode() {
        StringBuilder result = new StringBuilder(5);
        for (int index = 0; index < 5; index++) {
            result.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return result.toString();
    }

    public boolean matches(String submitted, Object expected) {
        return submitted != null
                && expected instanceof String code
                && submitted.trim().equalsIgnoreCase(code);
    }

    public BufferedImage render(String code) {
        BufferedImage image = new BufferedImage(170, 52, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(246, 248, 252));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
            for (int index = 0; index < code.length(); index++) {
                graphics.setColor(new Color(
                        35 + random.nextInt(70),
                        45 + random.nextInt(80),
                        80 + random.nextInt(80)
                ));
                graphics.drawString(String.valueOf(code.charAt(index)), 18 + index * 29, 36 + random.nextInt(5));
            }
            graphics.setColor(new Color(100, 120, 150, 120));
            for (int index = 0; index < 5; index++) {
                graphics.drawLine(
                        random.nextInt(image.getWidth()),
                        random.nextInt(image.getHeight()),
                        random.nextInt(image.getWidth()),
                        random.nextInt(image.getHeight())
                );
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }
}
