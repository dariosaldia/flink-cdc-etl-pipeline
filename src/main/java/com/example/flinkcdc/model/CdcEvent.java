package com.example.flinkcdc.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;

/**
 * Represents the Debezium payload envelope.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CdcEvent(
  String op,   // "c", "u", or "d"
  Order after, // the new state (null for deletes)
  Order before // the previous state (null for inserts)
) implements Serializable {}
