package com.fms.sakir.backtest.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class PopulateHistory implements Serializable {

    private String _id;
    private String _rev = null;
    private long lastExecuted;

    public PopulateHistory(String _id, long lastExecuted) {
        this._id = _id;
        this.lastExecuted = lastExecuted;
    }
}
