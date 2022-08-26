package com.fms.sakar.sakir.model.runner;

import com.binance.api.client.domain.market.CandlestickInterval;
import lombok.Builder;
import lombok.Data;
import org.ta4j.core.Trade;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Builder
@Data
public class RunnerRequest implements Serializable {

    @NotNull private String symbol;

    @NotNull private CandlestickInterval interval;

    @NotNull private String strategyName;
    
    private Boolean onlyFinalCandles;
}
