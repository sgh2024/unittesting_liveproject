package imagescaler;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ImageScalerTests {
    private ImageScaler imageScaler;
    private ImageScaleSpec scaleSpec;

    @BeforeEach
    void setUp() {
        imageScaler = new ImageScalerImplementation();
        scaleSpec = new ImageScaleSpec();
    }

    @Test
    public void setsDimensionsProperlyWhenFullySpecified() throws ImageScalerImplementation.Exception {
        final var image = getTestImage(200, 200);
        final var width = 100;
        final var height = 120;
        scaleSpec.setTargetWidth(width);
        scaleSpec.setTargetHeight(height);

        final var newImage = imageScaler.scale(image, scaleSpec);

        assertThat(newImage.getWidth(null), is(equalTo(width)));
        assertThat(newImage.getHeight(null), is(equalTo(height)));
    }

    @Test
    public void usesXDimensionWhenYNotSpecified() throws ImageScalerImplementation.Exception {
        final var image = getTestImage(200, 200);
        final var length = 50;
        scaleSpec.setTargetWidth(length);

        final var newImage = imageScaler.scale(image, scaleSpec);

        assertThat(newImage.getWidth(null), is(equalTo(length)));
        assertThat(newImage.getHeight(null), is(equalTo(length)));
    }

    @Test
    public void preservesAspectRatioWhenXDimensionSpecified() throws ImageScalerImplementation.Exception {
        final var image = getTestImage(200, 100);
        scaleSpec.setTargetWidth(50);

        final var newImage = imageScaler.scale(image, scaleSpec);

        assertThat(newImage.getWidth(null), is(equalTo(50)));
        assertThat(newImage.getHeight(null), is(equalTo(25)));
    }

    @Test
    public void usesYDimensionWhenXNotSpecified() throws ImageScalerImplementation.Exception {
        final var image = getTestImage(200, 200);
        final var scaleSpec = this.scaleSpec;
        final var length = 70;
        scaleSpec.setTargetHeight(length);

        final var newImage = imageScaler.scale(image, scaleSpec);

        assertThat(newImage.getWidth(null), is(equalTo(length)));
        assertThat(newImage.getHeight(null), is(equalTo(length)));
    }

    @Test
    public void doesNotTolerateAnOverlyLargeWidth() {
        final var maximumWidth = 200;
        final var maximumHeight = 100;
        final var image = getTestImage(maximumWidth, maximumHeight);
        scaleSpec.setTargetWidth(maximumWidth + 1);
        scaleSpec.setTargetHeight(maximumHeight);

        assertThrows(ImageScalerImplementation.Exception.class, () -> {
            imageScaler.scale(image, scaleSpec);
        });
    }

    @Test
    public void doesNotTolerateAnOverlyLargeHeight() {
        final var maximumHeight = 200;
        final var maximumWidth = 100;
        final var image = getTestImage(maximumWidth, maximumHeight);
        scaleSpec.setTargetWidth(maximumWidth);
        scaleSpec.setTargetHeight(maximumHeight + 1);

        assertThrows(ImageScalerImplementation.Exception.class, () -> {
            imageScaler.scale(image, scaleSpec);
        });
    }

    @Test
    public void doesNotTolerateMissingHeightAndWidth() {
        final var image = getTestImage(10, 10);

        assertThrows(ImageScalerImplementation.Exception.class, () -> {
            imageScaler.scale(image, scaleSpec);
        });
    }

    @Test
    public void scalesContentWhenDimensionsMatch() throws ImageScalerImplementation.Exception {
        final var originalDimension = 200;
        final var newDimension = 100;
        final var image = getTestImage(originalDimension, originalDimension);
        scaleSpec.setTargetWidth(newDimension);
        scaleSpec.setTargetHeight(newDimension);

        final var newImage = imageScaler.scale(image, scaleSpec);

        assertThat(newImage, isEquivalentTo(image.getScaledInstance(newDimension, newDimension, Image.SCALE_SMOOTH)));
    }

    @Test
    public void takesHorizontalCenterWhenImageTooWide() throws ImageScalerImplementation.Exception {
        final var newDimension = 50;
        final var image = getTestImage(200, 100);
        scaleSpec.setTargetWidth(newDimension);
        scaleSpec.setTargetHeight(newDimension);

        final var newImage = imageScaler.scale(image, scaleSpec);

        assertThat(newImage,
                isEquivalentTo(clip(
                        image.getScaledInstance(newDimension, newDimension, Image.SCALE_SMOOTH),
                        25, 0, newDimension, newDimension)));
    }

    @Test
    public void takesVerticalCenterWhenImageTooTall() throws ImageScalerImplementation.Exception {
        final var newDimension = 50;
        final var image = getTestImage(100, 200);
        scaleSpec.setTargetWidth(newDimension);
        scaleSpec.setTargetHeight(newDimension);

        final var newImage = imageScaler.scale(image, scaleSpec);

        assertThat(newImage,
                isEquivalentTo(clip(
                        image.getScaledInstance(newDimension, newDimension, Image.SCALE_SMOOTH),
                        0, 25, newDimension, newDimension)));
    }

    private Image clip(Image source, int x, int y, int width, int height) {
        final var result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final var graphics = result.createGraphics();
        graphics.drawImage(source, -x, -y, null);

        graphics.dispose();
        return result;
    }

    private static TypeSafeMatcher<Image> isEquivalentTo(Image expected) {
        final var expectedData = convertToBufferedImage(expected).getData().getDataBuffer();

        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(Image item) {
                final var actualData = convertToBufferedImage(item).getData().getDataBuffer();

                if (expectedData.getSize() != actualData.getSize()) {
                    return false;
                }

                for (var i = 0; i < expectedData.getSize(); ++i) {
                    if (expectedData.getElem(i) != actualData.getElem(i))
                        return false;
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is equivalent to supplied image");
            }
        };
    }

    private static BufferedImage convertToBufferedImage(Image image) {
        final var result = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        final var graphics = result.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        return result;
    }

    private static BufferedImage getTestImage(int width, int height) {
        final var result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final var graphics = result.createGraphics();
        graphics.setStroke(new BasicStroke(width / 10f));
        graphics.setPaint(Color.blue);
        graphics.drawLine(0, 0, width, height);
        graphics.dispose();

        return result;
    }
}
