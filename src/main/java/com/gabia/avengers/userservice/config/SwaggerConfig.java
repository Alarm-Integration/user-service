package com.gabia.avengers.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    private String version;
    private String title;

    @Bean
    public Docket apiV1() {
        version = "V1";
        title = "User Service API " + version;

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo(title, version))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.gabia"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo(String title, String version) {
        return new ApiInfoBuilder()
                .title(title)
                .description("Swagger로 생성한 User Service API 문서")
                .version(version)
                .contact(new Contact(
                        "Gabia",
                        "www.gabia.com",
                        "gabia@gabia.com"
                )).build();
    }
}
