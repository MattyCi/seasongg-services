package com.sgg.seasons.model;

import com.sgg.games.model.GameDao;
import com.sgg.rounds.model.RoundDao;
import com.sgg.users.UserDao;
import io.micronaut.data.annotation.DateCreated;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoundDao> rounds;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private GameDao game;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeasonStandingDao> standings;

    public void addRound(RoundDao round) {
        getRounds().add(round);
        round.setSeason(this);
    }

    /**
     * Replaces the current standings with the new standings provided.
     * <p>
     * This implementation is necessary because of the way Hibernate handles replacements for lists.
     * Instead of clearing and re-adding the new list, which can cause constraint violations, this method
     * first removes existing items that are not in the new list, then updates existing items or adds the
     * remaining new items.
     *
     * @param newStandings the list of new standings to replace the current ones with
     */
    public void replaceStandings(List<SeasonStandingDao> newStandings) {
        val currentStandingsMap = getStandings().stream()
                .collect(Collectors.toMap(s -> s.getUser().getUserId(), Function.identity()));
        val newStandingsMap = newStandings.stream()
                .collect(Collectors.toMap(s -> s.getUser().getUserId(), Function.identity()));
        val indicesToRemove = new ArrayList<Integer>();
        for (int i = 0; i < getStandings().size(); i++) {
            if (!newStandingsMap.containsKey(getStandings().get(i).getUser().getUserId())) {
                indicesToRemove.add(i);
            }
        }
        val toRemove = indicesToRemove.stream().map(i -> getStandings().get(i)).toList();
        if (!toRemove.isEmpty()) {
            getStandings().removeAll(toRemove);
        }
        for (SeasonStandingDao newStanding : newStandings) {
            if (currentStandingsMap.containsKey(newStanding.getUser().getUserId())) {
                val updateStanding = currentStandingsMap.get(newStanding.getUser().getUserId());
                updateStanding.setPlace(newStanding.getPlace());
                updateStanding.setPoints(newStanding.getPoints());
                updateStanding.setRoundsPlayed(newStanding.getRoundsPlayed());
            } else {
                addStanding(newStanding);
            }
        }
    }

    public void addStanding(SeasonStandingDao standing) {
        getStandings().add(standing);
        standing.setSeason(this);
    }
}
