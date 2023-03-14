package docsgenerator.exampleAPI.controllers.errors

import docsgenerator.exampleAPI.utils.ErrorFormat

object GlobalErrors {
    val INVALID_UUID = ErrorFormat(
        internalCode = 333,
        title = "Invalid UUID",
        detail = "The UUID provided is not valid"
    )
}
