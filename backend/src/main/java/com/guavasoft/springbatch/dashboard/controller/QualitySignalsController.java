package com.guavasoft.springbatch.dashboard.controller;

import com.guavasoft.springbatch.dashboard.model.QualitySignals;
import com.guavasoft.springbatch.dashboard.service.QualitySignalsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overview")
@RequiredArgsConstructor
@Tag(name = "Overview - Quality Signals", description = "Exception and quality signals for the dashboard overview page")
public class QualitySignalsController {

    private final QualitySignalsService qualitySignalsService;

    @GetMapping("/quality-signals")
    @Operation(summary = "Exception and quality signals", description = "Most recent failure label, processing totals, and last-updated timestamp.")
    public QualitySignals getQualitySignals() {
        return qualitySignalsService.getSignals();
    }
}
