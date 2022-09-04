package com.fms.sakir.backtest.model.tiingo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class OhlcResponse implements Serializable {

    private String ticker;

    private String baseCurrency;

    private String quoteCurrency;

    private List<OhlcData> priceData;
}
