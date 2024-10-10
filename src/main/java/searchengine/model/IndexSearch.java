package searchengine.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.*;

@Entity
@Table(name = "index_search")
@NoArgsConstructor
@Setter
@Getter
public class IndexSearch {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotNull
    @Column(name = "page_id")
    private int pageId;
    @NotNull
    @Column(name = "lemma_id")
    private int lemmaId;
    @Column(name = "lemma_rank")
    @NotNull
    private int lemmaCount;
    @ManyToOne(cascade = CascadeType.REMOVE)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "page_id", insertable = false, updatable = false, nullable = false)
    private Page page;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "lemma_id", insertable = false, updatable = false, nullable = false)
    private Lemma lemma;
}