package net.gunivers.dispenser.observer.raw_model;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.gunivers.dispenser.observer.raw_model.block_model.JsonBlockModelPlace;

public record JsonBlockModel(String parent,
                             @JsonProperty("ambientocclusion") boolean ambientOcclusion,
                             JsonBlockModelPlace display) {

}
