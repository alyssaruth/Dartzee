package dartzee.core.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun jsonMapper() = jacksonObjectMapper().apply {
    registerModule(KotlinModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    setSerializationInclusion(JsonInclude.Include.NON_NULL)
}