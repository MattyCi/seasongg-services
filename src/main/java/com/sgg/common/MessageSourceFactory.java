package com.sgg.common;

import io.micronaut.context.MessageSource;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import jakarta.inject.Singleton;

@Factory
class MessageSourceFactory {
    @Singleton
    MessageSource createMessageSource() {
        return new ResourceBundleMessageSource("validation.messages");
    }
}
