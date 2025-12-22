package com.sgg.rounds.model;

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


    private Double points;

}
