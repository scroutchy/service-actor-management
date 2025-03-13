package com.scr.project.sam.domains.security.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

//TODO: Investigate why need of this class in order to correctly orientate the choice of resolveArguments to the one of ReactivePageableHandlerMethodArgumentResolver
@Configuration
class WebConfig : WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(ReactivePageableHandlerMethodArgumentResolver())
    }
}