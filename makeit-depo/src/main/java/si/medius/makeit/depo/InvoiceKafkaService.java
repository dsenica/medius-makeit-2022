package si.medius.makeit.depo;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import io.smallrye.reactive.messaging.kafka.KafkaClientService;
import io.smallrye.reactive.messaging.kafka.KafkaProducer;
import io.smallrye.reactive.messaging.kafka.Record;
import si.medius.makeit.entity.Invoice;
import si.medius.makeit.entity.InvoiceStatus;

@ApplicationScoped
public class InvoiceKafkaService
{

    @Inject
    KafkaStreams streams;

    @Inject
    StreamConfig streamConfig;

    @Inject
    @Channel("invoices")
    Emitter<Record<String, Invoice>> invoiceEmitter;

    public void readyOrder(String key, Invoice invoice) {
        invoice.setStatus(InvoiceStatus.READY);
        invoiceEmitter.send(Record.of(key, invoice));
    }

    public List<KeyValue<String, Invoice>> getAll() {
        List<KeyValue<String, Invoice>> invoices = new ArrayList<>();
        var iterator = getInvoiceStateStore().all();
        while (iterator.hasNext()) {
            KeyValue<String, Invoice> i = iterator.next();
            if(i.value != null) {
                invoices.add(i);
            }
        }
        iterator.close();
        return invoices;
    }

    public Invoice getByKey(String key) {
        return getInvoiceStateStore().get(key);
    }

    private ReadOnlyKeyValueStore<String, Invoice> getInvoiceStateStore() {
        return streams.store(StoreQueryParameters.fromNameAndType(
                streamConfig.storeName(),
                QueryableStoreTypes.keyValueStore()
        ));
    }

}
