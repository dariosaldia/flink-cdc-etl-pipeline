package com.example.flinkcdc.serde;

import com.example.flinkcdc.model.CdcEvent;
import java.io.IOException;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.Collector;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class CdcEventDeserializationSchema implements KafkaRecordDeserializationSchema<CdcEvent> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public void deserialize(ConsumerRecord<byte[], byte[]> record,
      Collector<CdcEvent> out) throws IOException {
    JsonNode payload = mapper
        .readTree(record.value())
        .get("payload");

    // bind the entire payload into our CdcEvent record
    CdcEvent event = mapper.treeToValue(payload, CdcEvent.class);
    out.collect(event);
  }

  @Override
  public TypeInformation getProducedType() {
    return TypeInformation.of(new TypeHint<CdcEvent>() {});
  }
}
