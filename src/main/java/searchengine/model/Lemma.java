package searchengine.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import searchengine.config.Site;

import jakarta.persistence.*;

@Entity
@Table(name = "lemma", uniqueConstraints = @UniqueConstraint(columnNames = {"lemma","site_id"}))
@NoArgsConstructor
@Setter
@Getter
public class Lemma {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotNull
    private int frequency;
    @NotNull
    @Column(columnDefinition = "VARCHAR(255)")
    private String lemma;
    @NotNull
    @Column(name = "site_id")
    private int siteId;
    @ManyToOne(cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "site_id", insertable = false, updatable = false, nullable = false)
    private SitePage sitePage;

}
