package com.ravcube.lib.eureka;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@FeignClient
public @interface RavcubeEurekaClient {

    @AliasFor(annotation = FeignClient.class, attribute = "name")
    String name();

    @AliasFor(annotation = FeignClient.class, attribute = "path")
    String path() default "";
}
