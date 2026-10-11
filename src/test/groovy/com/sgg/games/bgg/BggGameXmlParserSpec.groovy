package com.sgg.games.bgg

import com.sgg.common.exception.NotFoundException
import com.sgg.common.exception.SggException
import spock.lang.Specification

class BggGameXmlParserSpec extends Specification {

    BggGameXmlParser parser = new BggGameXmlParser()

    def "parse a single game"() {
        given:
        def response = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="12345">
                    <thumbnail>https://example.com/game.png</thumbnail>
                    <name type="primary" value="Primary Name" />
                    <name type="alternate" value="Secondary Name 1" />
                    <name type="alternate" value="Secondary Name 2" />
                    <yearpublished value="2023"/>
                </item>
            </items>
        """.trim()

        when:
        def game = parser.parseSingleGame(response)

        then:
        game.gameId == 12345L
        game.name == "Primary Name"
        game.yearPublished == "2023"
        game.thumbnail == "https://example.com/game.png"
        game.rank == null
    }

    def "parse single game can read thumbnail from a value attribute"() {
        given:
        def response = """
            <items><item id="1">
                <name value="Game"/>
                <thumbnail value="https://example.com/attr.png"/>
            </item></items>
        """

        expect:
        parser.parseSingleGame(response).thumbnail == "https://example.com/attr.png"
    }

    def "parse single game leaves thumbnail null when it is empty"() {
        given:
        def response = """<items><item id="1"><name value="Game"/><thumbnail/></item></items>"""

        expect:
        parser.parseSingleGame(response).thumbnail == null
    }

    def "parse single game uses the first primary name regardless of element order"() {
        given:
        def response = """
            <items><item id="1">
                <name type="alternate" value="Alternate"/>
                <name type="primary" value="First Primary"/>
                <name type="primary" value="Second Primary"/>
            </item></items>
        """

        expect:
        parser.parseSingleGame(response).name == "First Primary"
    }

    def "parse single game treats a name without a type as primary"() {
        given:
        def response = """
            <items><item id="1">
                <name type="alternate" value="Alternate"/>
                <name value="Untyped"/>
            </item></items>
        """

        expect:
        parser.parseSingleGame(response).name == "Untyped"
    }

    def "parse single game falls back to the first alternate name when there is no primary"() {
        given:
        def response = """
            <items><item id="1">
                <name type="alternate" value="Alt One"/>
                <name type="alternate" value="Alt Two"/>
            </item></items>
        """

        expect:
        parser.parseSingleGame(response).name == "Alt One"
    }

    def "parse single game ignores unrecognized fields"() {
        given:
        def response = """
            <items><item id="1">
                <name value="Catan"/>
                <description>Some long description</description>
                <minplayers value="2"/>
            </item></items>
        """

        when:
        def game = parser.parseSingleGame(response)

        then:
        game.name == "Catan"
        game.gameId == 1L
    }

    def "parse single game throws NotFoundException when there are no items"() {
        when:
        parser.parseSingleGame("<items></items>")

        then:
        def e = thrown(NotFoundException)
        e.message == "Game not found."
    }

    def "parse single game throws when more than one item is returned"() {
        given:
        def response = """
            <items>
                <item id="1"><name value="One"/></item>
                <item id="2"><name value="Two"/></item>
            </items>
        """

        when:
        parser.parseSingleGame(response)

        then:
        def e = thrown(SggException)
        e.message == "BGG returned unexpected amount of games: 2"
    }

    def "parse single game throws when BGG returns an error element"() {
        given:
        def response = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item>
                    <error message='Invalid Item'/>
                </item>
            </items>
        """.trim()

        when:
        parser.parseSingleGame(response)

        then:
        def e = thrown(SggException)
        e.message == "Unexpected error returned from BGG."
    }

    def "parse single game throws when the error element has no message"() {
        when:
        parser.parseSingleGame("<items><item><error/></item></items>")

        then:
        def e = thrown(SggException)
        e.message == "Unexpected error returned from BGG."
    }

    def "parse single game throws when the item id is missing or not numeric"() {
        when:
        parser.parseSingleGame(response)

        then:
        def e = thrown(SggException)
        e.message == "BGG response missing game ID."

        where:
        response << [
                """<items><item><name value="No Id"/></item></items>""",
                """<items><item id="abc"><name value="Bad Id"/></item></items>"""
        ]
    }

    def "parse popular games maps every item with an id and rank"() {
        given:
        def response = """
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item id="1" rank="1">
                    <thumbnail value="https://example.com/game1.png"/>
                    <name value="Game 1"/>
                    <yearpublished value="2020"/>
                </item>
                <item id="2" rank="2">
                    <thumbnail value="https://example.com/game2.png"/>
                    <name value="Game 2"/>
                    <yearpublished value="2022"/>
                </item>
            </items>
        """

        when:
        def games = parser.parsePopularGames(response)

        then:
        games.size() == 2

        and:
        games[0].gameId == 1L
        games[0].rank == 1L
        games[0].name == "Game 1"
        games[0].yearPublished == "2020"
        games[0].thumbnail == "https://example.com/game1.png"

        and:
        games[1].gameId == 2L
        games[1].rank == 2L
        games[1].name == "Game 2"
        games[1].yearPublished == "2022"
        games[1].thumbnail == "https://example.com/game2.png"
    }

    def "parse popular games skips items with a missing or invalid id or rank"() {
        given:
        def response = """
            <items>
                <item id="1" rank="1"><name value="Valid"/></item>
                <item rank="2"><name value="No Id"/></item>
                <item id="3"><name value="No Rank"/></item>
                <item id="x" rank="4"><name value="Bad Id"/></item>
                <item id="5" rank="y"><name value="Bad Rank"/></item>
            </items>
        """

        when:
        def games = parser.parsePopularGames(response)

        then:
        games*.name == ["Valid"]
    }

    def "parse popular games returns an empty list when there are no items"() {
        expect:
        parser.parsePopularGames("<items></items>") == []
    }

    def "parse search results maps items"() {
        given:
        def response = """
            <items total="2" termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="13">
                    <name type="primary" value="Catan"/>
                    <yearpublished value="1995"/>
                </item>
                <item type="boardgame" id="27710">
                    <name type="primary" value="Catan: Cities &amp; Knights"/>
                    <yearpublished value="1998"/>
                </item>
            </items>
        """

        when:
        def games = parser.parseSearchResults(response)

        then:
        games*.gameId == [13L, 27710L]
        games*.name == ["Catan", "Catan: Cities & Knights"]
        games*.yearPublished == ["1995", "1998"]
        games*.rank == [null, null]
        games*.thumbnail == [null, null]
    }

    def "parse search results skips items without a valid id"() {
        given:
        def response = """
            <items>
                <item id="1"><name value="Valid"/></item>
                <item><name value="No Id"/></item>
                <item id="abc"><name value="Bad Id"/></item>
            </items>
        """

        expect:
        parser.parseSearchResults(response)*.name == ["Valid"]
    }

    def "parse search results returns an empty list when there are no items"() {
        expect:
        parser.parseSearchResults("""<items total="0"></items>""") == []
    }

    def "all parse methods reject an empty response"() {
        when:
        parser."$method"(response)

        then:
        def e = thrown(SggException)
        e.message == "Response from BGG was empty."

        where:
        [method, response] << [
                ["parseSingleGame", "parsePopularGames", "parseSearchResults"],
                [null, "", "   "]
        ].combinations()
    }

    def "all parse methods reject malformed XML"() {
        when:
        parser."$method"("<invalid>")

        then:
        def e = thrown(SggException)
        e.message == "Error parsing response from BGG."

        where:
        method << ["parseSingleGame", "parsePopularGames", "parseSearchResults"]
    }

    def "XML declaration preceded by whitespace is rejected as malformed"() {
        when:
        parser.parseSingleGame("\n  <?xml version=\"1.0\"?><items/>")

        then:
        def e = thrown(SggException)
        e.message == "Error parsing response from BGG."
    }

    def "documents containing a DOCTYPE are rejected (XXE protection)"() {
        given:
        def malicious = """
            <?xml version="1.0"?>
            <!DOCTYPE items [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
            <items><item id="1"><name value="&xxe;"/></item></items>
        """.trim()

        when:
        parser."$method"(malicious)

        then:
        def e = thrown(SggException)
        e.message == "Error parsing response from BGG."

        where:
        method << ["parseSingleGame", "parsePopularGames", "parseSearchResults"]
    }
}