package in.gov.npncd.caresetu.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "schemes")
public class Scheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "short_name", nullable = false, length = 80)
    private String shortName;

    @Column(name = "level_label", length = 80)
    private String levelLabel;

    @Column(name = "cover_text", nullable = false)
    private String coverText;

    @Column(name = "what_text", nullable = false)
    private String whatText;

    @Column(name = "how_text", nullable = false)
    private String howText;

    @Column(nullable = false)
    private boolean active = true;
}
