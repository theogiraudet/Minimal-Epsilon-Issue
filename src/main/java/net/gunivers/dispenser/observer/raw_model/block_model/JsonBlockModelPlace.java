package net.gunivers.dispenser.observer.raw_model.block_model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JsonBlockModelPlace(@JsonProperty("Position") BlockModelPosition position) {
}
