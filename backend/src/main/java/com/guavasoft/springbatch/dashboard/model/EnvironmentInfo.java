package com.guavasoft.springbatch.dashboard.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A configured datasource environment available to the dashboard.")
public record EnvironmentInfo(
    @Schema(description = "Display name of the environment.", example = "Localhost Postgres")
    String name,
    @Schema(description = "Database engine type derived from the JDBC URL.", example = "POSTGRESQL")
    String type
) {
}
