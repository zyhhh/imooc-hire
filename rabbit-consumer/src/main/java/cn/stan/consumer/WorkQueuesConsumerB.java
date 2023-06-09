package cn.stan.consumer;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 工作队列模式 消息接收
 */
public class WorkQueuesConsumerB {

    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.0.104");
        factory.setPort(5672);
        factory.setVirtualHost("/");
        factory.setUsername("imooc");
        factory.setPassword("imooc");

        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        channel.queueDeclare("work_queue", true, false, false, null);

        DefaultConsumer consumer = new DefaultConsumer(channel) {
            /*
                consumerTag– 与消费者关联的消费者标签
                envelope – 消息的打包数据
                properties – 消息的内容标头数据
                body – 消息正文（不透明，特定于客户端的字节数组）
             */
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) {
                System.out.println("body = " + new String(body));
            }
        };

        channel.basicConsume("work_queue", true, consumer);
    }

}
