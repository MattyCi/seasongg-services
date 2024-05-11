package common

import com.sgg.common.exception.NotFoundException
import com.sgg.users.UserRegistrationRequest
import com.sgg.users.UserService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
@Requires(env = "integ")
class DataBootstrap implements ApplicationEventListener<ServerStartupEvent> {

    @Inject
    UserService userService

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        try {
            userService.getUserByUsername("integ-user")
        } catch (NotFoundException ignored) {
            userService.registerUser(
                    new UserRegistrationRequest("integ-user", "test-pw", "test-pw")
            )
        }
    }
}
