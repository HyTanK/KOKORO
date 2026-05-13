package com.example.demo;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ImageInitializer {
    @PostConstruct
    public void initIcons() {
        try {
            File inputFile = new File("src/main/resources/static/images/icons.png");
            if (!inputFile.exists()) return;
            BufferedImage original = ImageIO.read(inputFile);
            int halfWidth = original.getWidth() / 2;
            ImageIO.write(original.getSubimage(0, 0, halfWidth, original.getHeight()), "png", new File("src/main/resources/static/images/sos.png"));
            ImageIO.write(original.getSubimage(halfWidth, 0, halfWidth, original.getHeight()), "png", new File("src/main/resources/static/images/hero.png"));
        } catch (Exception e) {}
    }
}
