package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.QualitySignals;
import com.guavasoft.springbatch.dashboard.service.QualitySignalsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overview")
@RequiredArgsConstructor
@Validated
@Tag(name = "Overview - Quality Signals", description = "Exception and quality signals for the dashboard overview page")
public class QualitySignalsController {

    private static final int DEFAULT_WINDOW_DAYS = 7;

    private final QualitySignalsService qualitySignalsService;

    @GetMapping("/quality-signals")
    @Operation(summary = "Exception and quality signals", description = "Most recent failure label, processing totals, and last-updated timestamp within the given lookback window.")
    public QualitySignals getQualitySignals(
            @RequestParam(defaultValue = "" + DEFAULT_WINDOW_DAYS) @Min(1) @Max(90) int window) {
        return qualitySignalsService.getSignals(window);
    }
}
