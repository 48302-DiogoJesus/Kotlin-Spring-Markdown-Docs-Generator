package docsgenerator.exampleAPI

import docsgenerator.docslib.generateDocs
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping


/**
 * Library clients would have to somehow get the RequestMappingInfoHandlerMapping to call my generator:
 * generateTypescriptSDKFromSpringHandlers
 */
@Component
class TypescriptSDKGenerator(requestMappingHandlerMapping: RequestMappingInfoHandlerMapping) {
    init {
        generateDocs(
            requestMappingHandlerMapping,
            buildDirectory = "./api-docs"
        )
    }
}
