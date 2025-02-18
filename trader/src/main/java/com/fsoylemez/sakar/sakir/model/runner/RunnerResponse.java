package com.fsoylemez.sakar.sakir.model.runner;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@Data
public class RunnerResponse implements Serializable {

    private UUID runnerId;

    private RunnerRequest runner;
}
