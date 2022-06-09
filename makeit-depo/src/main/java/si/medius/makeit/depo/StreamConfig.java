package si.medius.makeit.depo;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "stream")
public interface StreamConfig
{
    String storeName();
    String inTopic();
    String sourceName();
    String processorName();
}
