package com.netflix.productivity.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiErrorConfig {

    @Bean
    public OpenApiCustomiser standardErrorResponsesCustomiser() {
        return openAPI -> {
            Components components = openAPI.getComponents();
            if (components == null) {
                components = new Components();
                openAPI.setComponents(components);
            }

            Schema<?> errorSchema = new ObjectSchema()
                    .addProperty("success", new Schema<>().type("boolean").example(false))
                    .addProperty("status", new Schema<>().type("integer").format("int32").example(400))
                    .addProperty("message", new Schema<>().type("string").example("Validation failed"))
                    .addProperty("error", new Schema<>().type("object"))
                    .addProperty("errorCode", new Schema<>().type("string").example("VALIDATION_ERROR"))
                    .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
                    .addProperty("correlationId", new Schema<>().type("string").example("9f7b9b0f-2f2d-4f2a-9ad6-1f3d9dbe0a10"));

            components.addSchemas("StandardErrorResponse", errorSchema);

            ApiResponse badRequest = buildResponse("Bad Request", 400, "VALIDATION_ERROR");
            ApiResponse unauthorized = buildResponse("Unauthorized", 401, "AUTH_401_000");
            ApiResponse forbidden = buildResponse("Forbidden", 403, "AUTH_403_000");
            ApiResponse tooMany = buildResponse("Too Many Requests", 429, "RATE_LIMITED");
            ApiResponse internal = buildResponse("Internal Server Error", 500, "INTERNAL_ERROR");

            openAPI.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(op -> {
                ApiResponses responses = op.getResponses();
                if (!responses.containsKey("400")) responses.addApiResponse("400", badRequest);
                if (!responses.containsKey("401")) responses.addApiResponse("401", unauthorized);
                if (!responses.containsKey("403")) responses.addApiResponse("403", forbidden);
                if (!responses.containsKey("429")) responses.addApiResponse("429", tooMany);
                if (!responses.containsKey("500")) responses.addApiResponse("500", internal);
            }));
        };
    }

    private ApiResponse buildResponse(String title, int status, String code) {
        ApiResponse resp = new ApiResponse().description(title);
        Example ex = new Example().value("{\n  \"success\": false,\n  \"status\": " + status + ",\n  \"message\": \"" + title + "\",\n  \"error\": null,\n  \"errorCode\": \"" + code + "\",\n  \"timestamp\": \"2025-01-01T00:00:00\",\n  \"correlationId\": \"11111111-1111-1111-1111-111111111111\"\n}");
        MediaType mt = new MediaType().schema(new ObjectSchema().$ref("#/components/schemas/StandardErrorResponse")).addExamples("example", ex);
        Content content = new Content().addMediaType("application/json", mt);
        resp.setContent(content);
        return resp;
    }
}


