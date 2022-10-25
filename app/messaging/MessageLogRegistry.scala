package messaging

import akka.actor.ActorSystem
import play.api.Configuration
import akka.stream.Materializer
import akka.kafka.ConsumerSettings
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import play.api.Logger
import akka.kafka.Subscriptions
import akka.stream.scaladsl.Sink
import akka.kafka.scaladsl.Consumer
import akka.kafka.ProducerSettings
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.clients.producer.ProducerRecord

class MessageLogRegistry(configuration: Configuration, actorSystem: ActorSystem)(
    implicit val mat: Materializer)
    extends IMessageProcessingRegistry {
  private val log = Logger(this.getClass)
  private val bootstrapServers = configuration.get[String]("kafka.bootstrap.servers")
  private val offsetReset = configuration.get[String]("kafka.auto.offset.reset")

  private def consumerSettings(groupName: String) =
    ConsumerSettings(actorSystem, new ByteArrayDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)
      .withGroupId(groupName)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offsetReset)

  case class ConsumerParams(groupName: String, topics: Set[String])

  private val AllTopics = Set("tags")

  private def parseConsumerParams(queue: String): ConsumerParams = {
    val parts = queue.split("\\.")
    val topics = if (parts(1) == "*") AllTopics else Set(parts(1))
    ConsumerParams(parts(0), topics)
  }

  override def shutdown(): Unit = {}

  override def createProducer(topic: String): IMessageProducer = {
    val producerSettings =
      ProducerSettings(actorSystem, new ByteArraySerializer, new ByteArraySerializer)
        .withBootstrapServers(bootstrapServers)

    val producer = producerSettings.createKafkaProducer()
    (bytes: Array[Byte]) =>
      producer.send(new ProducerRecord(topic, bytes))
  }

  override def registerConsumer(queue: String, consumer: IMessageConsumer): Unit = {
    val ConsumerParams(groupName: String, topics: Set[String]) = parseConsumerParams(queue)
    Consumer
      .atMostOnceSource(consumerSettings(groupName), Subscriptions.topics(topics))
      .map { msg =>
        consumer.messageReceived(msg.value())
        msg
      }
      .toMat(Sink.ignore)(Consumer.DrainingControl.apply)
      .run()
  }
}
