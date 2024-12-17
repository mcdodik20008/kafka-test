package mcdodik;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Producer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        sendSingleMessage(producer);
//        sendManyMessages(producer);
        producer.close();
    }

    private static void sendSingleMessage(KafkaProducer<String, String> producer) {
        log.info("This class will produce messages to Kafka");

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("baza_java", "Arsen", "Spasibo");
        producer.send(producerRecord);
    }

    private static void sendManyMessages(KafkaProducer<String, String> producer) {
        List<Pair<String, String>> characters = new ArrayList<>();
        characters.add(Pair.with("Arsen", "Spasibo_many0"));
        characters.add(Pair.with("Arsen", "Spasibo_many1"));
        characters.add(Pair.with("Arsen", "Spasibo_many2"));
        characters.add(Pair.with("Arsen", "Spasibo_many3"));
        characters.add(Pair.with("Arsen", "Spasibo_many4"));
        characters.add(Pair.with(("Arsen"), "Spasibo_many5"));

        for (Pair<String, String> character : characters) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("baza_java", character.getValue0(), character.getValue1());

            producer.send(producerRecord, (RecordMetadata recordMetadata, Exception err) -> {
                if (err == null) {
                    log.info("Message received. \n" +
                            "topic [" + recordMetadata.topic() + "]\n" +
                            "partition [" + recordMetadata.partition() + "]\n" +
                            "offset [" + recordMetadata.offset() + "]\n" +
                            "timestamp [" + recordMetadata.timestamp() + "]");
                } else {
                    log.error("An error occurred while producing messages", err);
                }
            });

        }
    }
}