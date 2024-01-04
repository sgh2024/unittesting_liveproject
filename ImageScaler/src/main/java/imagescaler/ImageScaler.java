package imagescaler;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface ImageScaler {
    Image scale(Image image, ImageScaleSpec scaleSpec) throws ImageScalerImplementation.Exception;
}
