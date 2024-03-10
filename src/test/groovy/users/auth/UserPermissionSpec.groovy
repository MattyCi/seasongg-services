package users.auth

import com.sgg.SggAuthorizationSecurityRule
import com.sgg.SggSecurityRule
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.ServerAuthentication
import io.micronaut.security.rules.SecurityRuleResult
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.web.router.MethodBasedRouteMatch
import reactor.test.StepVerifier
import spock.lang.Specification

import static com.sgg.users.auth.PermissionType.WRITE
import static com.sgg.users.auth.ResourceType.SEASON

@MicronautTest(startApplication = false)
class UserPermissionSpec extends Specification {

    MethodBasedRouteMatch mockRouteMatch = Mock()
    HttpRequest mockHttpRequest = Mock()
    SggAuthorizationSecurityRule sggAuthorizationSecurityRule

    final AnnotationValue<SggSecurityRule> annotation = AnnotationValue.builder(SggSecurityRule.class)
            .member("resourceType", SEASON)
            .member("permissionType", WRITE)
            .member("resourceIdName", "seasonId")
            .build()

    def setup() {
        sggAuthorizationSecurityRule = new SggAuthorizationSecurityRule()
    }

    def 'should authorize user with proper permissions'() {
        given:
        final authentication = new ServerAuthentication(
                "user123", null, ["claims": ["SEASON:123": "WRITE"]]
        )

        when:
        final result = sggAuthorizationSecurityRule.check(mockHttpRequest,
                mockRouteMatch, authentication);

        then:
        1 * mockRouteMatch.hasAnnotation(SggSecurityRule.class) >> true
        1 * mockRouteMatch.getAnnotation(SggSecurityRule.class) >> annotation
        1 * mockRouteMatch.getVariableValues() >> ["seasonId": 123]
        0 * _
        StepVerifier.create(result)
                .expectNext(SecurityRuleResult.UNKNOWN)
                .expectComplete()
                .verify()
    }

    def 'should not authorize user with improper permissions'() {
        given:
        final authentication = new ServerAuthentication(
                "user123", null, ["claims": ["SEASON:123": "READ"]]
        )

        when:
        final result = sggAuthorizationSecurityRule.check(mockHttpRequest,
                mockRouteMatch, authentication);

        then:
        1 * mockRouteMatch.hasAnnotation(SggSecurityRule.class) >> true
        1 * mockRouteMatch.getAnnotation(SggSecurityRule.class) >> annotation
        1 * mockRouteMatch.getVariableValues() >> ["seasonId": 123]
        0 * _
        StepVerifier.create(result)
                .expectNext(SecurityRuleResult.REJECTED)
                .expectComplete()
                .verify()
    }

    def 'should reject if not authenticated for a request that requires some sort of authorization'() {
        given:
        final authentication = null

        when:
        final result = sggAuthorizationSecurityRule.check(mockHttpRequest,
                mockRouteMatch, authentication);

        then:
        1 * mockRouteMatch.hasAnnotation(SggSecurityRule.class) >> true
        0 * _
        StepVerifier.create(result)
                .expectNext(SecurityRuleResult.REJECTED)
                .expectComplete()
                .verify()
    }

    def 'should throw exception for misconfigured authz annotation'() {
        given:
        final authentication = new ServerAuthentication(
                "user123", null, ["claims": ["SEASON:123": "READ"]]
        )
        final misconfiguredAnnotation = AnnotationValue.builder(SggSecurityRule.class)
                .member("resourceType", SEASON)
                .member("permissionType", WRITE)
                // .member("resourceIdName", "seasonId")
                .build()

        when:
        sggAuthorizationSecurityRule.check(mockHttpRequest,
                mockRouteMatch, authentication);

        then:
        1 * mockRouteMatch.hasAnnotation(SggSecurityRule.class) >> true
        1 * mockRouteMatch.getAnnotation(SggSecurityRule.class) >> misconfiguredAnnotation
        0 * _
        thrown(RuntimeException)
    }

    def 'should continue processing if request does not require authz'() {
        given:
        final authentication = new ServerAuthentication(
                "user123", null, ["claims": ["SEASON:123": "WRITE"]]
        )

        when:
        final result = sggAuthorizationSecurityRule.check(mockHttpRequest,
                mockRouteMatch, authentication);

        then:
        1 * mockRouteMatch.hasAnnotation(SggSecurityRule.class) >> false
        0 * _
        StepVerifier.create(result)
                .expectNext(SecurityRuleResult.UNKNOWN)
                .expectComplete()
                .verify()
    }
}
