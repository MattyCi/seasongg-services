package com.sgg.rounds.model;

import com.sgg.seasons.model.SeasonDao;
import com.sgg.users.UserDao;
import io.micronaut.data.annotation.DateCreated;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_id")
    private Long roundId;

    @DateCreated
    @Column(name = "round_date", updatable = false, nullable = false)
    private OffsetDateTime roundDate;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundResultDao> roundResults;

    @ManyToOne
    @JoinColumn(name="season_id")
    private SeasonDao season;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserDao creator;
}
