package assetmetadataregistry;

import javax.persistence.*;

@Entity(name = "assets_metadata")
@Table(name = "assets")
public class AssetMetadataRecord {
    @Id()
    @Column(name = "asset_id")
    private String id;
    @Column(name = "asset_title_en")
    private String titleEnglish;
    @Column(name = "asset_title_de")
    private String titleGerman;
    @Column(name = "asset_title_es")
    private String titleSpanish;
    @Column(name = "asset_title_fr")
    private String titleFrench;
    @Column(name = "asset_desc_en", columnDefinition = "clob")
    private String descriptionEnglish;
    @Column(name = "asset_desc_de", columnDefinition = "clob")
    private String descriptionGerman;
    @Column(name = "asset_desc_es", columnDefinition = "clob")
    private String descriptionSpanish;
    @Column(name = "asset_desc_fr", columnDefinition = "clob")
    private String descriptionFrench;
    @Column(name = "thumb_64", columnDefinition = "clob")
    private String thumb64;
    @Column(name = "thumb_128", columnDefinition = "clob")
    private String thumb128;
    @Column(name = "thumb_256", columnDefinition = "clob")
    private String thumb256;
    @Column(name = "thumb_512", columnDefinition = "clob")
    private String thumb512;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getTitleEnglish() {
        return this.titleEnglish;
    }

    public String getTitleGerman() {
        return this.titleGerman;
    }

    public String getTitleSpanish() {
        return this.titleSpanish;
    }

    public String getTitleFrench() {
        return this.titleFrench;
    }

    public void setTitleEnglish(String value) {
        this.titleEnglish = value;
    }

    public void setTitleGerman(String value) {
        this.titleGerman = value;
    }

    public void setTitleSpanish(String value) {
        this.titleSpanish = value;
    }

    public void setTitleFrench(String value) {
        this.titleFrench = value;
    }

    public void setDescriptionEnglish(String value) {
        this.descriptionEnglish = value;
    }

    public String getDescriptionEnglish() {
        return descriptionEnglish;
    }

    public void setDescriptionGerman(String value) {
        this.descriptionGerman = value;
    }

    public String getDescriptionGerman() {
        return descriptionGerman;
    }

    public void setDescriptionSpanish(String value) {
        this.descriptionSpanish = value;
    }

    public String getDescriptionSpanish() {
        return descriptionSpanish;
    }

    public void setDescriptionFrench(String value) {
        this.descriptionFrench = value;
    }

    public String getDescriptionFrench() {
        return descriptionFrench;
    }

    public String getThumb64() {
        return thumb64;
    }

    public void setThumb64(String value) {
        this.thumb64 = value;
    }

    public String getThumb128() {
        return thumb128;
    }

    public void setThumb128(String value) {
        this.thumb128 = value;
    }

    public String getThumb256() {
        return thumb256;
    }

    public void setThumb256(String value) {
        this.thumb256 = value;
    }

    public String getThumb512() {
        return thumb512;
    }

    public void setThumb512(String value) {
        this.thumb512 = value;
    }
}
