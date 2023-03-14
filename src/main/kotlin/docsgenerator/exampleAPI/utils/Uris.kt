package docsgenerator.exampleAPI.utils

object Uris {
    const val BASE = "/api"

    object Users {
        const val BASE = "${Uris.BASE}/users"

        const val CREATE = "/"
        const val GET = "/{id}"
        const val DELETE = "/{id}"
    }

    object Posts {
        const val BASE = "${Uris.BASE}/posts"

        const val GET = "/{id}"
        const val CREATE = "/"
    }
}
