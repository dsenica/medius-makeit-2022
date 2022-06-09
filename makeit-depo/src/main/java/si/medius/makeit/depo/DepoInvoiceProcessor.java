package si.medius.makeit.depo;

import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueStore;
import org.eclipse.microprofile.config.ConfigProvider;

import si.medius.makeit.entity.Invoice;
import si.medius.makeit.entity.InvoiceStatus;

public class DepoInvoiceProcessor implements Processor<String, Invoice, String, Invoice>
{

    private ProcessorContext<String, Invoice> context;
    private KeyValueStore<String, Invoice> kvStore;

    @Override
    public void init(ProcessorContext<String, Invoice> processorContext)
    {
        context = processorContext;
        kvStore = context.getStateStore(ConfigProvider.getConfig().getValue("stream.store-name", String.class));
    }

    @Override
    public void process(Record<String, Invoice> record)
    {
        if (record.value().getStatus() == InvoiceStatus.PENDING)
        {
            kvStore.put(record.value().getInvoiceNo(), record.value());
        }
        else if (record.value().getStatus() == InvoiceStatus.READY)
        {
            kvStore.delete(record.value().getInvoiceNo());
        }
        // context.forward(); // If we wish to send data to some other topic
    }
}
