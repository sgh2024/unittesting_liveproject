package assetregistry;

import javax.persistence.*;

@Entity(name = "assets")
@Table(name = "assets")
public class AssetRecord {
    @Id
    @Column(name = "asset_id")
    private String id;

    @Column(name = "full_url", columnDefinition = "clob")
    private String fullUrl;

    @Column(name = "asset_title_en")
    private String titleEnglish;

    @Column(name = "asset_title_de")
    private String titleGerman;

    @Column(name = "asset_title_es")
    private String titleSpanish;

    @Column(name = "asset_title_fr")
    private String titleFrench;

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public String getTitleEnglish() {
        return titleEnglish;
    }

    public void setTitleEnglish(String titleEnglish) {
        this.titleEnglish = titleEnglish;
    }

    public String getTitleGerman() {
        return titleGerman;
    }

    public void setTitleGerman(String titleGerman) {
        this.titleGerman = titleGerman;
    }

    public String getTitleSpanish() {
        return titleSpanish;
    }

    public void setTitleSpanish(String titleSpanish) {
        this.titleSpanish = titleSpanish;
    }

    public String getTitleFrench() {
        return titleFrench;
    }

    public void setTitleFrench(String titleFrench) {
        this.titleFrench = titleFrench;
    }
}
