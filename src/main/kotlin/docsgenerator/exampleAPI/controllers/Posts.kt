package docsgenerator.exampleAPI.controllers

import docsgenerator.exampleAPI.controllers.errors.GlobalErrors
import docsgenerator.exampleAPI.controllers.errors.PostErrors
import docsgenerator.exampleAPI.controllers.errors.UserErrors
import docsgenerator.exampleAPI.utils.Uris
import docsgenerator.types.HandlerResponse
import docsgenerator.types.HandlerResponseType
import docsgenerator.types.ResponseStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

data class Post(
    val id: UUID,

    val author: User,

    val title: String,
    val content: String?,

    val createdAt: Instant = Instant.now()
)

val POSTS_DB: MutableMap<UUID, Post> = mutableMapOf()

@RestController
@RequestMapping(Uris.Posts.BASE)
class Posts {

    /**
     * ! Example using your custom response types !
     * ResponseEntity<Post> means you can only return responses where the body is User
     * Using ResponseEntity<HandlerResponse<Post, YourErrorFormat>> is an alternative we built
     * */
    @GetMapping(Uris.Posts.GET)
    fun get(
        @PathVariable id: String
    ): ResponseEntity<Post> {
        /**
         * ! The errors thrown here are sent as 500 to the client if not intercepted and handled
         * This is only to show you can return your own types
         * Using this approach you should have a postHandlerProcessor interceptor to handle errors
         * and emit them to the client after being properly formatted
         */
        val uuid = UUID.fromString(id)
            ?: throw Error(GlobalErrors.INVALID_UUID.toString())

        val post: Post =
            POSTS_DB[uuid]
                ?: throw Error(PostErrors.POST_NOT_FOUND_ERROR.toString())

        return ResponseEntity.ok().body(post)
    }

    data class CreatePostModel(val title: String, val content: String?, val authorId: String)

    @ResponseStatus(
        success = [HttpStatus.CREATED],
        error = [HttpStatus.UNPROCESSABLE_ENTITY, HttpStatus.NOT_FOUND]
    )
    @PostMapping(Uris.Posts.CREATE)
    fun create(
        @RequestBody post: Posts.CreatePostModel
    ): HandlerResponseType<Post> {
        if (post.title.length <= 4 || (post.content != null && post.content.length <= 4))
            return HandlerResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY,
                PostErrors.POST_PARAMS_LENGTH_ERROR
            )

        val authorUUID = UUID.fromString(post.authorId)
            ?: return HandlerResponse.error(
                HttpStatus.UNPROCESSABLE_ENTITY,
                GlobalErrors.INVALID_UUID
            )

        val author: User =
            USERS_DB[authorUUID]
                ?: return HandlerResponse.error(
                    HttpStatus.NOT_FOUND,
                    UserErrors.USER_NOT_FOUND_ERROR
                )

        val newPostId = UUID.randomUUID()
        val newPost: Post =
            Post(
                id = newPostId,
                author = author,
                title = post.title,
                content = post.content
            )

        // Persist user
        POSTS_DB[newPostId] = newPost

        return HandlerResponse.success(newPost)
    }
}
