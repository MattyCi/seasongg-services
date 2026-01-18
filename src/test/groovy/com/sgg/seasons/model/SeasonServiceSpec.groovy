package com.sgg.seasons.model

import com.sgg.DaoFixtures
import com.sgg.common.exception.ClientException
import com.sgg.common.exception.NotFoundException
import com.sgg.common.exception.SggException
import com.sgg.games.ExternalGameClient
import com.sgg.games.GameRepository
import com.sgg.games.GameService
import com.sgg.games.model.GameDao
import com.sgg.games.model.GameDto
import com.sgg.games.model.GameMapper
import com.sgg.rounds.RoundMapper
import com.sgg.rounds.model.RoundDao
import com.sgg.rounds.model.RoundDto
import com.sgg.rounds.model.RoundResultDao
import com.sgg.rounds.model.RoundResultDto
import com.sgg.seasons.ScoringService
import com.sgg.seasons.SeasonRepository
import com.sgg.seasons.SeasonService
import com.sgg.users.UserDao
import com.sgg.users.UserMapper
import com.sgg.users.UserMapperImpl
import com.sgg.users.UserService
import com.sgg.users.authz.PermissionService
import com.sgg.users.model.UserDto
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import spock.lang.Specification

import java.time.OffsetDateTime

import static com.sgg.DtoFixtures.*

@MicronautTest(startApplication = false)
class SeasonServiceSpec extends Specification {
    SeasonRepository seasonRepository = Mock()
    UserService userService = Mock()
    SeasonMapper seasonMapper = new SeasonMapperImpl()
    Validator validator = Mock()
    UserMapper userMapper = new UserMapperImpl()
    PermissionService permissionService = Mock()
    GameService gameService = Mock(constructorArgs: [Mock(GameRepository), Mock(GameMapper), Mock(ExternalGameClient)])
    RoundMapper roundMapper = Mock()
    ScoringService scoringService = Mock()

    SeasonService seasonService = new SeasonService(
            seasonRepository,
            gameService,
            userService,
            seasonMapper,
            validator,
            userMapper,
            permissionService,
            roundMapper,
            scoringService
    )

    def "should get season"() {
        given:
        def dao = SeasonDao.builder().seasonId(123).build()

        when:
        def result = seasonService.getSeason("123")

        then:
        1 * seasonRepository.findById(123) >> Optional.of(dao)
        0 * _
        result.seasonId == 123
    }

    def "should throw not found for season that doesn't exist"() {
        when:
        seasonService.getSeason("123")

        then:
        1 * seasonRepository.findById(123) >> Optional.empty()
        0 * _
        def e = thrown(NotFoundException)
        e.message == "No season was found for the given ID."
    }

    def "should throw error for invalid input for season ID"() {
        when:
        seasonService.getSeason("123invalid")

        then:
        0 * _
        def e = thrown(ClientException)
        e.message == "Invalid seasonId provided."
    }

    def "should use game from game service in create season response"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def seasonDao = SeasonDao.builder()
                .seasonId(123)
                .name("season name")
                .creator(creator)
                .game(new GameDao(gameId: 123, name: "bad name"))
                .build()
        def seasonDto = seasonMapper.toSeasonDto(seasonDao)
        def game = new GameDto(gameId: 123, name: "Catan") // correct name from game service

        when:
        def result = seasonService.createSeason(seasonDto)

        then:
        1 * userService.getCurrentUser() >> UserDto.builder().userId(123L).username("matty").build()
        1 * validator.validate(seasonDto) >> []
        1 * seasonRepository.findByNameIgnoreCase(seasonDao.name) >> Optional.empty()
        1 * gameService.maybeCreateGame(_) >> game
        1 * seasonRepository.save({ SeasonDao s ->
            assert s.game.name == game.name // good name from BGG persisted
        }) >> seasonDao
        1 * permissionService.insertSeasonAdminPermission(_, _)
        0 * _
    }

    def "should update season admin and season end date"() {
        given:
        def oldAdmin = UserDao.builder().userId(123).username("old-admin").build()
        def newAdmin = UserDao.builder().userId(456).username("new-admin").build()
        def oldSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(oldAdmin)
                .startDate(OffsetDateTime.parse("2999-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("3000-01-01T00:00:00-00:00"))
                .status("ACTIVE")
                .name("Season")
                .build()
        def newSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(newAdmin)
                .startDate(OffsetDateTime.parse("2999-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("2000-01-01T00:00:00-00:00"))
                .status("ACTIVE") // should change to inactive based on new end date
                .name("Season")
                .build()
        def newSeasonDto = seasonMapper.toSeasonDto(newSeason)

        when:
        seasonService.updateSeason("999", newSeasonDto)

        then:
        1 * validator.validate(newSeasonDto) >> []
        1 * seasonRepository.findById(999) >> Optional.of(oldSeason)
        1 * userService.getUserById(456) >> userMapper.userToUserDto(newAdmin)
        1 * permissionService.swapSeasonAdmins(999, { UserDao o ->
            o.userId == 123
            o.username == "old-admin"
        }, { UserDao n ->
            n.userId == 456
            n.username == "new-admin"
        })

        and: "season has been updated with new admin and inactivated due to date change"
        1 * seasonRepository.update({ SeasonDao s ->
            s.creator.userId == 456
            s.status == "INACTIVE"
        }) >> newSeason
        0 * _
    }

    def "should throw if new season admin doesn't exist"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def oldSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(creator)
                .build()
        def newSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(new UserDao(userId: 404))
                .build()
        def newSeasonDto = seasonMapper.toSeasonDto(newSeason)

        when:
        seasonService.updateSeason("999", newSeasonDto)

        then:
        1 * validator.validate(newSeasonDto) >> []
        1 * seasonRepository.findById(999) >> Optional.of(oldSeason)
        1 * userService.getUserById(404) >> { throw new NotFoundException("User not found!") }
        0 * _
        def e = thrown(NotFoundException)
        e.message == "The given user for the season admin does not exist."
    }

    def "should re-activate season if season end date is extended past the current date"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def oldSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(creator)
                .startDate(OffsetDateTime.parse("2000-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("2020-01-01T00:00:00-00:00"))
                .status("INACTIVE")
                .name("Catan Tournament")
                .build()
        def newSeason = SeasonDao.builder()
                .seasonId(999)
                .creator(creator)
                .startDate(OffsetDateTime.parse("2000-01-01T00:00:00-00:00"))
                .endDate(OffsetDateTime.parse("3000-01-01T00:00:00-00:00"))
                .status("INACTIVE")
                .name("Catan Tournament")
                .build()
        def newSeasonDto = seasonMapper.toSeasonDto(newSeason)

        when:
        def updated = seasonService.updateSeason("999", newSeasonDto)

        then:
        1 * validator.validate(newSeasonDto) >> []
        1 * seasonRepository.findById(999) >> Optional.of(oldSeason)

        and: "season should be re-activated"
        1 * seasonRepository.update({ SeasonDao s ->
            s.status == "ACTIVE"
        }) >> newSeason

        and:
        0 * _
        updated.endDate == OffsetDateTime.parse("3000-01-01T00:00:00-00:00")
    }

    def "should delete season"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def season = new SeasonDao(
                seasonId: 999,
                creator: creator
        )

        when:
        seasonService.deleteSeason(season.seasonId.toString())

        then:
        1 * seasonRepository.findById(999) >> Optional.of(season)
        1 * seasonRepository.delete(season)
    }

    def "should throw if season not found on season delete"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def season = new SeasonDao(
                seasonId: 999,
                creator: creator
        )

        when:
        seasonService.deleteSeason(season.seasonId.toString())

        then:
        1 * seasonRepository.findById(999) >> Optional.empty()
        def e = thrown(NotFoundException)
        e.message == "No season was found for the given ID."
    }

    def "should throw if unexpected error occurs on season delete"() {
        given:
        def creator = new UserDao(userId: 123, username: "matty")
        def season = new SeasonDao(
                seasonId: 999,
                creator: creator
        )

        when:
        seasonService.deleteSeason(season.seasonId.toString())

        then:
        1 * seasonRepository.findById(999) >> Optional.of(season)
        1 * seasonRepository.delete(season) >> {
            throw new Exception("Unexpected!")
        }
        def e = thrown(Exception)
        e.message == "An unexpected error occurred trying to delete your season, please try again."
    }

    def "should delete round"() {
        given:
        def season = SeasonDao.builder()
                .seasonId(123)
                .build()
        def rounds = [
                RoundDao.builder().roundId(123).season(season).build(),
                RoundDao.builder().roundId(456).season(season).build()
        ]
        season.rounds = rounds

        when:
        seasonService.removeRound("123", "456")

        then:
        1 * seasonRepository.findById(123L) >> Optional.of(season)
        1 * seasonRepository.update({ SeasonDao s ->
            assert s.rounds.size() == 1
            assert s.rounds[0].getRoundId() == 123
        })
        0 * _
    }

    def "should not delete round if season doesn't exist"() {
        when:
        seasonService.removeRound("404", "404")

        then:
        1 * seasonRepository.findById(404) >> Optional.empty()
        0 * _
        def e = thrown(NotFoundException)
        e.message == "No season was found for the given ID."
    }

    def "should not delete round if round not in season"() {
        given:
        def season = SeasonDao.builder()
                .seasonId(1)
                .build()
        def rounds = [ RoundDao.builder().roundId(123).season(season).build() ]
        season.rounds = rounds

        when:
        seasonService.removeRound("1", "404")

        then:
        1 * seasonRepository.findById(1L) >> Optional.of(season)
        0 * _
        def e = thrown(NotFoundException)
        e.message == "The round does not exist for the given season."
    }

    def "should handle unexpected exception for delete round"() {
        given:
        def season = SeasonDao.builder()
                .seasonId(1)
                .build()
        def rounds = [ RoundDao.builder().roundId(123).season(season).build() ]
        season.rounds = rounds

        when:
        seasonService.removeRound("1", "123")

        then:
        1 * seasonRepository.findById(1L) >> Optional.of(season)
        1 * seasonRepository.update(_) >> { throw new Exception() }
        0 * _
        def e = thrown(SggException)
        e.message == "An unexpected error occurred trying to remove round, please try again."
    }

    def "should add round to season"() {
        given:
        def season = DaoFixtures.validSeason()
        def round = roundWith(
                [
                        roundResult(10, matty()),
                        roundResult(9, aaron()),
                        roundResult(8, mistalegit())
                ]
        )
        def roundDao = DaoFixtures.roundWith(
                [
                        DaoFixtures.roundResult(10, DaoFixtures.matty()),
                        DaoFixtures.roundResult(9, DaoFixtures.aaron()),
                        DaoFixtures.roundResult(8, DaoFixtures.mistalegit())
                ]
        )

        when:
        def result = seasonService.addRound("1", round)

        then:
        1 * seasonRepository.findById(1) >> Optional.of(season)
        1 * userService.getCurrentUser() >> matty()
        1 * validator.validate(round) >> []
        1 * roundMapper.toRoundDao(round) >> roundDao
        3 * scoringService.calculatePoints(_)
        3 * userService.getUserById(_) >> new UserDto()
        1 * scoringService.calculateStandings(season) >> [
                new SeasonStandingDao(place: 1, points: 10, user: DaoFixtures.matty(), season: season),
                new SeasonStandingDao(place: 2, points: 9, user: DaoFixtures.aaron(), season: season),
                new SeasonStandingDao(place: 3, points: 8, user: DaoFixtures.mistalegit(), season: season)
        ]
        1 * seasonRepository.save({ s ->
            season.standings.size() == 3
            season.standings[0].user.username == "matty"
            season.standings[1].user.username == "aaron"
            season.standings[2].user.username == "mistalegit"
        }) >> season
        0 * _
        result.seasonId == 1
        result.standings.size() == 3
    }

    def "should not add round if season doesn't exist"() {
        when:
        seasonService.addRound("1", new RoundDto())

        then:
        1 * seasonRepository.findById(1L) >> Optional.empty()
        0 * _
        def e = thrown(NotFoundException)
        e.message == "No season was found for the given ID."
    }

    def "should not add round if season inactive"() {
        given:
        def season = SeasonDao.builder()
                .seasonId(1)
                .status("INACTIVE")
                .build()

        when:
        seasonService.addRound("1", new RoundDto())

        then:
        1 * seasonRepository.findById(1L) >> Optional.of(season)
        0 * _
        def e = thrown(ClientException)
        e.message == "Rounds cannot be added because the season has ended."
    }

    def "should throw client exception when a player does not exist"() {
        given:
        def resultDto1 = RoundResultDto.builder().place(1)
                .user(UserDto.builder().userId(123).username("some-user").build())
                .build()
        def resultDto2 = RoundResultDto.builder().place(2)
                .user(UserDto.builder().userId(404).username("non-existent").build())
                .build()
        def roundDto = RoundDto.builder()
                .roundResults([resultDto1, resultDto2])
                .build()
        def resultDao1 = RoundResultDao.builder().place(1)
                .user(UserDao.builder().userId(123).username("some-user").build())
                .build()
        def resultDao2 = RoundResultDao.builder().place(2)
                .user(UserDao.builder().userId(404).username("non-existent").build())
                .build()
        def roundDao = RoundDao.builder()
                .roundResults([resultDao1, resultDao2])
                .build()

        when:
        seasonService.addRound("1", roundDto)

        then:
        1 * seasonRepository.findById(1) >> Optional.of(SeasonDao.builder().seasonId(1L).name("season").status("ACTIVE").build())
        1 * userService.getCurrentUser() >> UserDto.builder().userId(99L).username("creator").build()
        1 * validator.validate(roundDto) >> []
        1 * roundMapper.toRoundDao(roundDto) >> roundDao
        2 * scoringService.calculatePoints(_)
        1 * userService.getUserById(123) >> new UserDto()
        1 * userService.getUserById(404L) >> { throw new NotFoundException("User not found!") }
        0 * _
        def e = thrown(ClientException)
        e.message == "Failed to create round because the player non-existent in place 2 does not exist."
    }
}
