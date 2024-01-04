package assetsubjectsregistry;

import javax.persistence.*;

@Entity(name = "asset_subjects_subject")
@Table(name = "subjects")
public class SubjectRecord {
    @Id
    @Column(name = "subject_id")
    private String id;

    @Column(name = "subject_title", columnDefinition = "clob")
    private String title;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
