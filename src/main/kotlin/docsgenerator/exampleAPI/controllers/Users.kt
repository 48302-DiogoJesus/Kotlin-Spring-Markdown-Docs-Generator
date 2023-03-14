package docsgenerator.exampleAPI.controllers

import docsgenerator.exampleAPI.controllers.errors.GlobalErrors
import docsgenerator.exampleAPI.controllers.errors.UserErrors
import docsgenerator.exampleAPI.utils.Uris
import docsgenerator.types.ResponseStatus
import docsgenerator.types.HandlerResponse
import docsgenerator.types.HandlerResponseType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,

    val name: String,
    val createdAt: Instant = Instant.now()
)

val USERS_DB: MutableMap<UUID, User> = mutableMapOf()

@RestController
@RequestMapping(Uris.Users.BASE)
class Users {

    @ResponseStatus(
        // success = [OK] // is the default
        error = [
            HttpStatus.NOT_FOUND,
            HttpStatus.UNPROCESSABLE_ENTITY
        ]
    )
    @GetMapping(Uris.Users.GET)
    fun get(
        // (name = "id") is not needed if the variable name is "id" instead of "userId"
        // Here it's just used to show it's possible
        @PathVariable(name = "id") userId: String,
    ): HandlerResponseType<User> {
        val uuid = UUID.fromString(userId)
            ?: return HandlerResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY,
                GlobalErrors.INVALID_UUID
            )

        val user: User =
            USERS_DB[uuid]
                ?: return HandlerResponse.error(
                    HttpStatus.NOT_FOUND,
                    UserErrors.USER_NOT_FOUND_ERROR
                )

        return HandlerResponse.success(user)
    }

    data class CreateUserModel(val name: String)

    @ResponseStatus(
        success = [HttpStatus.CREATED],
        error = [
            HttpStatus.NOT_FOUND,
            HttpStatus.UNPROCESSABLE_ENTITY
        ]
    )
    @PostMapping(Uris.Users.CREATE)
    fun create(
        @RequestBody user: Users.CreateUserModel
    ): HandlerResponseType<User> {
        if (user.name.length <= 4) {
            return HandlerResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY,
                UserErrors.USER_NAME_LENGTH_ERROR
            )
        }

        val newUserId = UUID.randomUUID()
        val newUser: User =
            User(
                id = newUserId,
                name = user.name
                // createdAt automatically generated
            )

        // Persist user
        USERS_DB[newUserId] = newUser

        return HandlerResponse.success(newUser, HttpStatus.CREATED)
    }

    @ResponseStatus(
        success = [HttpStatus.NO_CONTENT],
        error = [
            HttpStatus.UNPROCESSABLE_ENTITY,
            HttpStatus.NOT_FOUND
        ]
    )
    @DeleteMapping(Uris.Users.DELETE)
    fun delete(
        @PathVariable(name = "id") userId: String
    ): HandlerResponseType<Unit> {
        val uuid = UUID.fromString(userId)
            ?: return HandlerResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY,
                GlobalErrors.INVALID_UUID
            )

        USERS_DB[uuid]
            ?: return HandlerResponse.error(
                HttpStatus.NOT_FOUND,
                UserErrors.USER_NOT_FOUND_ERROR
            )

        // Remove from DB
        USERS_DB.remove(uuid)

        return HandlerResponse.success(Unit, HttpStatus.NO_CONTENT)
    }
}
