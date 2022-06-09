package si.medius.makeit.depo;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.kafka.streams.KeyValue;

import si.medius.makeit.entity.Invoice;

@Path("invoices")
public class InvoiceResource
{
    @Inject
    InvoiceKafkaService invoiceKafkaService;

    @GET
    public List<KeyValue<String, Invoice>> getAll()
    {
        return invoiceKafkaService.getAll();
    }

    @GET
    @Path("/{key}")
    public Invoice getByKey(@PathParam("key") String key) {
        return invoiceKafkaService.getByKey(key);
    }

    @POST
    @Path("/{key}")
    public Response ready(@PathParam("key") String key) {
        Invoice invoice = invoiceKafkaService.getByKey(key);
        if(invoice == null) {
            return Response.notModified().build();
        }
        invoiceKafkaService.readyOrder(key, invoice);
        return Response.accepted().build();
    }
}
