package website.tma.mouserec.configs;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverPartition;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import website.tma.mouserec.model.MouseEvent;

import java.util.List;
import java.util.UUID;

@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String TOPIC_NAME = "events";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    @Primary
    public NewTopic newTopic() {
        return TopicBuilder.name(TOPIC_NAME)
                .compact()
                .build();
    }

    @Bean
    public KafkaSender<UUID, MouseEvent> kafkaSender() {
        return KafkaSender.create(SenderOptions
                .<UUID, MouseEvent>create()
                .producerProperty("bootstrap.servers", bootstrapServers)
                .producerProperty("key.serializer", "org.apache.kafka.common.serialization.UUIDSerializer")
                .producerProperty("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer")
        );
    }

    @Bean
    public KafkaReceiver<UUID, MouseEvent> kafkaReceiver() {
        return KafkaReceiver.create(ReceiverOptions
                .<UUID, MouseEvent>create()
                .subscription(List.of(TOPIC_NAME))
                .addAssignListener(partitions -> partitions.forEach(ReceiverPartition::seekToBeginning))
                .consumerProperty("bootstrap.servers", bootstrapServers)
                .consumerProperty("group.id", UUID.randomUUID().toString())
                .consumerProperty("client.id", "event-consumer")
                .consumerProperty("spring.json.trusted.packages", "*")
                .consumerProperty("auto.offset.reset", "earliest")
                .consumerProperty("key.deserializer", "org.apache.kafka.common.serialization.UUIDDeserializer")
                .consumerProperty("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer")
        );
    }

}
