package com.rtech.mandiplus.dto;

public class GovMandiForecastMetricsDTO {

    private Double sufficientHistoryCoveragePercent;
    private Long totalTrackedCommodities;
    private Long commoditiesWithSufficientData;
    private String baselineModel;
    private String evaluationSummary;

    public GovMandiForecastMetricsDTO() {
    }

    public GovMandiForecastMetricsDTO(Double sufficientHistoryCoveragePercent, Long totalTrackedCommodities,
                                      Long commoditiesWithSufficientData, String baselineModel, String evaluationSummary) {
        this.sufficientHistoryCoveragePercent = sufficientHistoryCoveragePercent;
        this.totalTrackedCommodities = totalTrackedCommodities;
        this.commoditiesWithSufficientData = commoditiesWithSufficientData;
        this.baselineModel = baselineModel;
        this.evaluationSummary = evaluationSummary;
    }

    public Double getSufficientHistoryCoveragePercent() {
        return sufficientHistoryCoveragePercent;
    }

    public void setSufficientHistoryCoveragePercent(Double sufficientHistoryCoveragePercent) {
        this.sufficientHistoryCoveragePercent = sufficientHistoryCoveragePercent;
    }

    public Long getTotalTrackedCommodities() {
        return totalTrackedCommodities;
    }

    public void setTotalTrackedCommodities(Long totalTrackedCommodities) {
        this.totalTrackedCommodities = totalTrackedCommodities;
    }

    public Long getCommoditiesWithSufficientData() {
        return commoditiesWithSufficientData;
    }

    public void setCommoditiesWithSufficientData(Long commoditiesWithSufficientData) {
        this.commoditiesWithSufficientData = commoditiesWithSufficientData;
    }

    public String getBaselineModel() {
        return baselineModel;
    }

    public void setBaselineModel(String baselineModel) {
        this.baselineModel = baselineModel;
    }

    public String getEvaluationSummary() {
        return evaluationSummary;
    }

    public void setEvaluationSummary(String evaluationSummary) {
        this.evaluationSummary = evaluationSummary;
    }
}
