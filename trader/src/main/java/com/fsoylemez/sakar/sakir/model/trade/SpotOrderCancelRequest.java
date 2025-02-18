package com.fsoylemez.sakar.sakir.model.trade;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SpotOrderCancelRequest implements Serializable {

    @NotNull private String symbol;

    @NotNull private Long orderId;
}
