package assetingest;

import java.awt.*;
import java.io.OutputStream;

public interface AssetStoreEntry {
    String getUri();

    void saveImage(Image image);
}
