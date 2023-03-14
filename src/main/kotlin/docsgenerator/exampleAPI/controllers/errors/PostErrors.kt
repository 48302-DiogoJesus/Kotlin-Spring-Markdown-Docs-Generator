package docsgenerator.exampleAPI.controllers.errors

import docsgenerator.exampleAPI.utils.ErrorFormat

object PostErrors {
    val POST_NOT_FOUND_ERROR = ErrorFormat(
        internalCode = 444,
        title = "Post does not exist",
        detail = "The post id provided does not correspond to an existing post"
    )

    val POST_PARAMS_LENGTH_ERROR = ErrorFormat(
        internalCode = 555,
        title = "Invalid post title/content length",
        detail = "The post title/content length is not within bounds. It should be greater than 4 characters"
    )
}