package assetingest;

import com.amazonaws.services.s3.*;
import com.amazonaws.services.s3.model.*;

import javax.imageio.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

public class S3AssetStore implements AssetStore {

    private final AmazonS3 client;
    private final String bucketName;

    public S3AssetStore(AmazonS3 client, String bucketName) {
        this.client = client;
        this.bucketName = bucketName;
        client.createBucket(bucketName);
    }

    @Override
    public AssetStoreEntry allocateEntry() {

        try {
            final var key = UUID.randomUUID().toString();
            return new AssetStoreEntryImplementation(key);
        } catch (Exception e) {
            return null;
        }
    }

    private class AssetStoreEntryImplementation implements AssetStoreEntry {

        private final String key;

        public AssetStoreEntryImplementation(String key) {
            this.key = key;
        }

        @Override
        public String getUri() {
            return client.getUrl(bucketName, key).toExternalForm();
        }

        @Override
        public void saveImage(Image image) {
            try {
                final var buffer = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                final var graphics = buffer.getGraphics();
                graphics.drawImage(image, 0, 0, null);

                final var outputStream = new ByteArrayOutputStream();
                ImageIO.write(buffer, "png", outputStream);

                final var metadata = new ObjectMetadata();
                metadata.setContentType("image/png");
                client.putObject(bucketName, key, new ByteArrayInputStream(outputStream.toByteArray()), metadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
