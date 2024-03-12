package com.sgg.seasons.model;

import com.sgg.users.UserDao;
import io.micronaut.data.annotation.DateCreated;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

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

    private String name;

    @DateCreated
    @Column(name = "start_date", updatable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @ManyToOne
    @JoinColumn
    private UserDao creator;

    @Column(length = 1)
    private String status;

    // TODO: add rounds

    // TODO: add game

    // TODO: add season standings
}
