package com.sgg.seasons;

import com.sgg.common.exception.SggException;
import com.sgg.rounds.model.RoundResultDao;
import com.sgg.seasons.model.SeasonDao;
import com.sgg.seasons.model.SeasonStandingDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Singleton
@Slf4j
@AllArgsConstructor(onConstructor_ = @Inject)
public class ScoringService {

    private static final Integer INELIGIBLE_PLACE = 999;

    public void calculatePoints(RoundResultDao result) {
        // TODO: for now, basic scoring algorithm. in future multiple possible scoring systems allowed
        double points;
        int place = result.getPlace();
        points = switch (place) {
            case 1 -> 10;
            case 2 -> 9;
            case 3 -> 8;
            case 4 -> 7;
            case 5 -> 6;
            case 6 -> 5;
            case 7 -> 4;
            case 8 -> 3;
            case 9 -> 2;
            default -> 1;
        };
        result.setPoints(points);
    }

    public List<SeasonStandingDao> calculateStandings(SeasonDao season) {
        try {
            val standings = new ArrayList<SeasonStandingDao>();
            val allResults = season.getRounds().stream().flatMap(r -> r.getRoundResults().stream()).toList();
            populateStandings(season, allResults, standings);
            averagePoints(standings);
            setIneligiblePlayers(standings);
            return determinePlaces(standings);
        } catch (Exception e) {
            log.error("Unexpected error occurred calculating season standings", e);
            throw new SggException("Unexpected error occurred calculating season standings. Please try again.");
        }
    }

    private void populateStandings(SeasonDao season, List<RoundResultDao> allResults,
                                   List<SeasonStandingDao> standings) {
        allResults.forEach( rr -> {
            val standing = standings.stream()
                    .filter(s -> s.getUser().getUserId().equals(rr.getUser().getUserId()))
                    .findFirst();
            if (standing.isPresent()) {
                standing.get().setPoints(standing.get().getPoints() + rr.getPoints());
                standing.get().setRoundsPlayed(standing.get().getRoundsPlayed() + 1);
            } else {
                val newStanding = SeasonStandingDao.builder()
                        .user(rr.getUser())
                        .season(season)
                        .points(rr.getPoints())
                        .roundsPlayed(1)
                        .build();
                standings.add(newStanding);
            }
        });
    }

    private List<SeasonStandingDao> determinePlaces(ArrayList<SeasonStandingDao> standings) {
        val sortedStandings = standings.stream()
                .sorted(Comparator.comparingDouble(SeasonStandingDao::getPoints).reversed())
                .toList();
        sortedStandings.get(0).setPlace(1);
        for (int i = 0; i < sortedStandings.size(); i++) {
            if (i > 0) {
                val currentStanding = sortedStandings.get(i);
                if (INELIGIBLE_PLACE.equals(currentStanding.getPlace())) {
                    continue;
                }
                val previousStanding = sortedStandings.get(i - 1);
                if (currentStanding.getPoints().equals(previousStanding.getPoints())) {
                    currentStanding.setPlace(previousStanding.getPlace());
                } else {
                    currentStanding.setPlace(i + 1);
                }
            }
        }
        return sortedStandings;
    }

    private void averagePoints(ArrayList<SeasonStandingDao> standings) {
        for (SeasonStandingDao standing : standings) {
            Double averagePoints = standing.getPoints() / standing.getRoundsPlayed();
            standing.setPoints(roundAveragePoints(averagePoints));
        }
    }

    private double roundAveragePoints(Double totalAveragePoints) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN);
        return Double.parseDouble(df.format(totalAveragePoints));
    }

    private void setIneligiblePlayers(ArrayList<SeasonStandingDao> standings) {
        Integer minimumRequiredGames = determineMinimumRequiredGames(standings);
        for (SeasonStandingDao standing : standings) {
            if (!isEligibleToCompete(standing, minimumRequiredGames)) {
                standing.setPlace(INELIGIBLE_PLACE);
            }
        }
    }

    private static Integer determineMinimumRequiredGames(ArrayList<SeasonStandingDao> standings) {
        int sumOfGamesPlayedForAllPlayers = standings.stream()
                .mapToInt(SeasonStandingDao::getRoundsPlayed)
                .sum();
        if (sumOfGamesPlayedForAllPlayers == 0) {
            return 1;
        }
        var totalPlayers = standings.size();
        var rounded = Math.ceil((double) (sumOfGamesPlayedForAllPlayers / totalPlayers) / 2);
        return Math.max(1, (int) rounded);
    }

    private boolean isEligibleToCompete(SeasonStandingDao standing, Integer minimumRequiredGames) {
        return standing.getRoundsPlayed() >= minimumRequiredGames;
    }
}
