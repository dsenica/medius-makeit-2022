package si.medius.makeit.depo;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.kafka.client.serialization.ObjectMapperSerde;
import si.medius.makeit.entity.Invoice;
import si.medius.makeit.entity.InvoiceStatus;

@ApplicationScoped
public class DepoStream
{
    private static final Logger log = LoggerFactory.getLogger(DepoStream.class);

    @Inject
    StreamConfig streamConfig;

    @Produces
    public Topology depoStream() {
        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapperSerde<Invoice> invoiceSerde = new ObjectMapperSerde<>(Invoice.class);

        Map<String, String> changeLogConfig = new HashMap<>();

        StoreBuilder<KeyValueStore<String, Invoice>> stateStore = Stores.keyValueStoreBuilder(
                        Stores.persistentKeyValueStore(streamConfig.storeName()),
                Serdes.String(),
                invoiceSerde
                )
                .withLoggingEnabled(changeLogConfig)
                .withCachingEnabled();

        final Topology topology = builder.build();
        topology.addSource(streamConfig.sourceName(), Serdes.String().deserializer(), invoiceSerde.deserializer(), streamConfig.inTopic());
        topology.addProcessor(streamConfig.processorName(), DepoInvoiceProcessor::new, streamConfig.sourceName());
        topology.addStateStore(stateStore, streamConfig.processorName());
        log.info("Stream started");
        return topology;
    }
}
