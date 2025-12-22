package com.sgg.rounds.model;

import com.sgg.users.UserDao;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "round_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoundResultDao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_result_id")
    private Long roundResultsId;

    private Long place;

    private Double points;

    @ManyToOne
    @JoinColumn(name="round_id", nullable = false)
    private RoundDao round;

    @ManyToOne
    private UserDao user;
}
