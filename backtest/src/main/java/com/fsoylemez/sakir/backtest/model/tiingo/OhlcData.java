package com.fsoylemez.sakir.backtest.model.tiingo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fsoylemez.sakir.backtest.serializer.DateToLongDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class OhlcData implements Serializable {

    private String _id;

    private String _rev;

    @JsonDeserialize(using = DateToLongDeserializer.class)
    private long date;

    private Double open;

    private Double high;

    private Double low;

    private Double close;

    private Double volume;

    private Double volumeNotional;

    private Long tradesDone;
}
