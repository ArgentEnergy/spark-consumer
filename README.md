# spark-consumer
An example of a message consumer that uses Apache Spark streaming to connect to Apache Kafka to read messages. The 
consumer does basic analytics on the message content and saves the data to a database. The consumer can be run under 
Mesos Marathon.

Configuration to external resources are provided in Apache ZooKeeper.

The consumer can be run directly from the JAR on a local machine as the Spark dependencies and database driver are 
bundled in the JAR. In a production application, those JARs would be under a provided scope in the pom.xml file.

There were no unit tests written because after researching online, it seems that the Spark framework makes it hard for 
developers to test code. There was one library that I found but it creates a Spark local instance, which isn't unit 
testing.

## Database Schema
Database used is PostgreSQL and launched inside a Docker container using Kitematic.

```sql
-- Commands run as the postgres user
CREATE USER sparky ENCRYPTED PASSWORD 'sparkconsumerdbuser';
CREATE DATABASE spark_sample WITH OWNER = 'sparky';
-- Run under the spark_sample database as the postgres user
CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;

-- Commands run as the sparky user
CREATE TABLE message (
  id UUID PRIMARY KEY,
  word VARCHAR(45) NOT NULL,
  word_count INTEGER NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
```

## Apache Zookeeper
Docker was used to run an instance of Zookeeper using Kitematic. A GUI called ZooNavigator was also run in Kitematic 
to provide the ability to create the needed application configuration.

### DB Configuration
Node name is db.
```text
spring.datasource.url=jdbc:postgresql://172.17.0.1:5432/spark_sample
spring.datasource.username=sparky
spring.datasource.password=sparkconsumerdbuser
spring.datasource.driver-class-name=org.postgresql.Driver
```

### Apache Kafka Configuration
Node name is kafka.
```text
brokers=172.17.0.1:9092
topics=messages0,messages1
```

## Apache Kafka
Docker was used to run an instance of Apache Kafka (ches/kafka) using Kitematic along with Kafka-Manager 
(sheepkiller/kafka-manager). Used Kafka-Manager to create the cluster, topic(s), and assign the broker to the topic(s).

Had to add ZOOKEEPER_CONNECTION_STRING=172.17.0.1:2181 to Kitematic for the Kafka container to run.

Here's an example of a message format used:
```text
This is a message
This is a text document
Follow me
This will be the last line
```

Command to populate Kafka with messages:
```text
echo "This is a message\nThis is a text document\nFollow me\nThis will be the last line" > /tmp/msg1.txt
./kafka-console-producer.sh --broker-list localhost:9092 --topic messages0 < /tmp/msg1.txt
```

Above was run inside the Kafka docker container. The Kafka Console Producer script interprets each line as a message.

## Sample Output
```text
id,word,word_count,created_at
59de27f5-7b86-4852-af12-0d06f63ced85,Follow,1,2017-08-23 13:44:29
19730138-31a5-4d25-bb28-73dd6dd4361b,Follow,1,2017-08-23 13:44:48
073c10e5-c461-4633-811d-1711b2bfddbb,This,3,2017-08-23 13:44:29
d8301648-d19b-4a7f-bbfa-053b0c951987,This,3,2017-08-23 13:44:49
7d073d6d-4641-4309-bab4-5291c3b38e1e,a,2,2017-08-23 13:44:29
c0bd33fe-79e9-416b-8509-563dd8626afb,a,2,2017-08-23 13:44:48
84089445-0b7b-405b-b723-2a46a91f0192,be,1,2017-08-23 13:44:29
c8013438-eeb2-4d1c-a779-d40b8ac6a9d1,be,1,2017-08-23 13:44:48
019e80d9-a58a-4428-8520-cd2fc2192d5e,document,1,2017-08-23 13:44:28
b27c5332-f216-4cd2-9b43-939252af42a9,document,1,2017-08-23 13:44:48
24da1141-802d-43a7-886c-c9b946d6fced,is,2,2017-08-23 13:44:28
1d74bad5-5efd-430c-947d-3640a784e028,is,2,2017-08-23 13:44:48
0bd29f07-22d6-4080-bc8f-7d3cf567a063,last,1,2017-08-23 13:44:28
15a993d9-4b7a-44bc-9638-1768db37e3ed,last,1,2017-08-23 13:44:48
7f8ffea8-90ce-4552-82f1-1257b8e55a52,line,1,2017-08-23 13:44:29
df0a3525-1ae9-4e05-9974-a860dca9f27c,line,1,2017-08-23 13:44:49
1db13a96-9f43-4f59-bfcf-495f2ba03ba2,me,1,2017-08-23 13:44:28
2346c817-1c99-4966-9a86-7da9a6e7bb12,me,1,2017-08-23 13:44:48
a8a1a91d-e872-4f78-aac6-c64aa86ef83e,message,1,2017-08-23 13:44:28
9a0fd359-ded0-4efd-b6ef-f0cf33da2a76,message,1,2017-08-23 13:44:48
a9784038-33af-4bb8-ba7a-0478fa3f81e1,text,1,2017-08-23 13:44:29
550e1eae-ef44-4e80-a045-6d37f6dcae67,text,1,2017-08-23 13:44:48
9fceeff7-5ce5-488e-b1de-e2c27fb3258c,the,1,2017-08-23 13:44:29
e5b41560-7f17-4f35-bfca-d7c2a0c1d8fe,the,1,2017-08-23 13:44:48
369d9794-159e-4cbe-9d41-75dcfa999562,will,1,2017-08-23 13:44:29
cf485564-2eca-4b02-9aea-fa604beffdd4,will,1,2017-08-23 13:44:48
```

The output above shows that two duplicate messages have been streamed at different times.
