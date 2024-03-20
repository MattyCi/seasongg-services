package com.sgg.games.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sgg.seasons.model.SeasonDao;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "games", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "UQ_gamename"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDao {

    @Id
    @Column(name = "game_id", unique = true)
    private Long gameId;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "game")
    @JsonIgnore
    private List<SeasonDao> seasons;
}
