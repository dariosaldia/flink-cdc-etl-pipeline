# Stage 1: build the fat‑jar
FROM maven:3-amazoncorretto-17-alpine AS builder
WORKDIR /workspace

# copy only pom + sources to leverage layer caching
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: runtime image
FROM flink:2.0.0-java17

# copy the built jar into Flink's user lib dir
COPY --from=builder /workspace/target/flink-cdc-etl-pipeline-1.0-SNAPSHOT.jar \
     /opt/flink/usrlib/

# expose the Flink web UI
EXPOSE 8081

ENTRYPOINT ["bash","-c","\
  # forward signals -> stop cluster cleanly
  trap '/opt/flink/bin/stop-cluster.sh; exit 0' SIGTERM SIGINT; \
  \
  # start JM and TM in the foreground so logs go to stdout
  /opt/flink/bin/jobmanager.sh start-foreground & \
  /opt/flink/bin/taskmanager.sh start-foreground & \
  \
  # submit the job (this blocks, streaming your job’s logs too)
  exec /opt/flink/bin/flink run \
    -m localhost:8081 \
    -c com.example.flinkcdc.OrdersCdcToParquetJob \
    /opt/flink/usrlib/flink-cdc-etl-pipeline-1.0-SNAPSHOT.jar \
"]
