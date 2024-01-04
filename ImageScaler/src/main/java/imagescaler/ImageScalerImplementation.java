package imagescaler;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageScalerImplementation implements ImageScaler {
    @Override
    public BufferedImage scale(Image image, ImageScaleSpec scaleSpec) throws Exception {
        final var originalWidth = image.getWidth(null);
        final var originalHeight = image.getHeight(null);
        final var originalAspectRatio = (1f * originalWidth) / originalHeight;

        if (scaleSpec.getTargetWidth() == null && scaleSpec.getTargetHeight() == null) {
            throw new Exception();
        }

        int newWidth;
        int newHeight;
        final var preserveAspectRatio = scaleSpec.getTargetWidth() == null ||
                scaleSpec.getTargetHeight() == null;

        if (!preserveAspectRatio) {
            newWidth = scaleSpec.getTargetWidth();
            newHeight = scaleSpec.getTargetHeight();
        } else {
            if (scaleSpec.getTargetHeight() == null) {
                newWidth = scaleSpec.getTargetWidth();
                newHeight = (int) (newWidth / originalAspectRatio);
            } else if (scaleSpec.getTargetWidth() == null) {
                newHeight = scaleSpec.getTargetHeight();
                newWidth = (int) (newHeight * originalAspectRatio);
            } else {
                throw new Exception();
            }
        }

        if (newWidth > originalWidth || newHeight > originalHeight) {
            throw new Exception();
        }

        final var widthScale = 1f * newWidth / originalWidth;
        final var heightScale = 1f * newHeight / originalHeight;

        final var scaled = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        final var result = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        final var graphics = result.createGraphics();
        final var dx = getImageOffsetInDimension(originalWidth, newWidth, widthScale, heightScale);
        final var dy = getImageOffsetInDimension(originalHeight, newHeight, heightScale, widthScale);
        graphics.drawImage(scaled, dx, dy, null);

        graphics.dispose();

        return result;
    }

    private int getImageOffsetInDimension(int originalMeasurement, int newMeasurement, float thisDimensionScale, float otherDimensionScale) {
        if (thisDimensionScale < otherDimensionScale) {
            final var idealMeasurement = originalMeasurement * otherDimensionScale;
            final var surplusMeasurement = idealMeasurement - newMeasurement;
            return (int) -surplusMeasurement / 2;
        }
        return 0;
    }

    private static int coalesce(Integer l, Integer r) {
        if (l != null) {
            return l;
        }

        return r;
    }

    public static class Exception extends java.lang.Exception {
    }
}
