package com.sgg.seasons.model;

import com.sgg.games.model.GameDao;
import com.sgg.users.UserDao;
import io.micronaut.data.annotation.DateCreated;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "seasons", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "UQ_name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_id")
    private Long seasonId;

    @Column(nullable = false, unique = true)
    private String name;

    @DateCreated
    @Column(name = "start_date", updatable = false, nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserDao creator;

    @Column(length = 32, nullable = false)
    private String status;

    // TODO: add rounds
    private String rounds;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private GameDao game;

    // TODO: add season standings
}
