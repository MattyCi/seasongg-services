package users.auth

import com.sgg.SggAuthorizationSecurityRule
import com.sgg.SggSecurityRule
import com.sgg.users.auth.ResourceType
import com.sgg.users.model.PermissionDto
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

    def setup() {
        sggAuthorizationSecurityRule = new SggAuthorizationSecurityRule()
    }

    def 'should authorize user with proper permissions'() {
        given:
        final permission = PermissionDto.builder()
            .permissionType(WRITE)
            .resourceType(SEASON)
            .resourceId(123).build()
        final authentication = new ServerAuthentication(
                "user123", null, ["claims": [permission]]
        )
        final annotation = AnnotationValue.builder(SggSecurityRule.class)
                .member("resourceType", SEASON)
                .member("permissionType", WRITE)
                .member("resourceIdName", "seasonId")
                .build()

        when:
        final result = sggAuthorizationSecurityRule.check(mockHttpRequest, mockRouteMatch, authentication);

        then:
        1 * mockRouteMatch.hasAnnotation(SggSecurityRule.class) >> true
        1 * mockRouteMatch.getAnnotation(SggSecurityRule.class) >> annotation
        1 * mockRouteMatch.getVariableValues() >> ["seasonId": 123]
        StepVerifier.create(result)
                .expectNext(SecurityRuleResult.ALLOWED)
                .expectComplete()
                .verify()
    }

}
