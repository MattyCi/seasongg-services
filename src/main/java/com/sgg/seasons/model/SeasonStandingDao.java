package com.sgg.seasons.model;

import com.sgg.users.UserDao;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "season_standings", uniqueConstraints = @UniqueConstraint(columnNames = { "season_id", "user_id" }, name = "UQ_name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonStandingDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "season_standing_id")
    private Long seasonStandingId;

    @Column(nullable = false)
    private Integer place;

    @Column(nullable = false)
    private Double points;

    @Column(name="rounds_played", nullable = false)
    private int roundsPlayed;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDao user;

    @ManyToOne
    @JoinColumn(name="season_id", nullable = false)
    private SeasonDao season;
}
