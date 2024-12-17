Установим кафку в доккер

[docker-compose.yaml](docker-compose.yaml)

Подключимся к контейнеру (можно через интерфейс доккер десктопа)

/usr/bin/kafka-topics - исполняемый файл, с помощью которого можно творить всякое.

Для теста я советую просто сидишнуться и исполнять его по названию, а не по полному пути
(Смысл добавлять его в патх)

* Управление топиками

Создадим топик:
```bash
kafka-topics --bootstrap-server localhost:9092 --create --topic baza
```

Выводит инфо про топик
```bash
kafka-topics --bootstrap-server localhost:9092 --describe --topic baza
```

Выведем лист топиков

```bash
kafka-topics --bootstrap-server localhost:9092 --list
```

Удалим топик
```bash
kafka-topics --bootstrap-server localhost:9092 --delete --topic baza
```

Создадим топик разбитый на партиции
```bash
kafka-topics --bootstrap-server localhost:9092 --create --topic baza --partitions 3
```

* Продюсинг

Наспамим сообщения через kafka-console-producer
```bash
kafka-console-producer --bootstrap-server localhost:9092 --topic baza
```

Создадим второй топик baza2 и наспамим туда с ключами
```bash
kafka-console-producer --bootstrap-server localhost:9092 --topic baza2 --property parse.key=true --property key.separator=:
```

* Консьюминг

Обычный:
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic baza --from-beginning
```

С доп информацие (ключи, значения, таймстампом и тп)
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic baza --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.timestamp=true --property print.key=true --property print.value=true
```
При выводе базы - ключи должны быть нулл, а вот при выводе базы 2 ключи должны быть ключами.

* Группы конзумеров

Создадим топик для них
```bash
kafka-topics --bootstrap-server localhost:9092 --topic baza_group --create --partitions 3
```

Создадим первого чела-читателя
```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic baza_group --group baza_group_cunsomers
```


Детские игры закончились, базируем базу в java приложении

```bash
kafka-topics --bootstrap-server localhost:9092 --topic baza_java --create --partitions 3
```

депендесисы для работы с кафкой без спринга
```xml
<dependencies>
    <dependency>
        <groupId>org.apache.kafka</groupId>
        <artifactId>kafka-clients</artifactId>
        <version>3.7.1</version>
    </dependency>
</dependencies>
```

Создадим продюсера
```java
public class Producer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
//        sendSingleMessage(producer);
    }

    private static void sendSingleMessage(KafkaProducer<String, String> producer) {
        log.info("This class will produce messages to Kafka");

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("baza_java", "Arsen", "Spasibo");
        producer.send(producerRecord);
        producer.close();
    }
}
```

Запустим код и посмотрим в консоли доккера что пришло

Теперь жава консумер напишем

```java
public class Consumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    public static void main(String[] args) {
        log.info("This class consumes messages from Kafka");

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "baza_java");
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        String topic = "baza_java";
        consumer.subscribe(Arrays.asList(topic));

        while(true){
            ConsumerRecords<String, String> messages = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> message : messages){
                log.info("key [" + message.key() + "] value [" + message.value() +"]");
                log.info("partition [" + message.partition() + "] offset [" + message.offset() + "]");
            }
        }
    }
}
```