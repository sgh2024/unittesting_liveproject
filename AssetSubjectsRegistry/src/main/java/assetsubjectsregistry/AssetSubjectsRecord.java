package assetsubjectsregistry;


import javax.persistence.*;
import java.util.*;

@Entity(name = "asset_subjects_asset")
@Table(name = "assets")
public class AssetSubjectsRecord {
    @Id()
    @Column(name = "asset_id")
    private String id;

    @ManyToMany(targetEntity = SubjectRecord.class)
    @JoinTable(
            name = "assets_subjects",
            joinColumns = { @JoinColumn( name = "asset_id")},
            inverseJoinColumns = { @JoinColumn(name = "subject_id", columnDefinition = "subject_id")}
    )
    private Set<SubjectRecord> subjects;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<SubjectRecord> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<SubjectRecord> subjects) {
        this.subjects = subjects;
    }
}
