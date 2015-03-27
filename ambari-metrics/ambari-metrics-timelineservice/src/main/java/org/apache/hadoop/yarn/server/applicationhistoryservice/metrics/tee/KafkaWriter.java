/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.yarn.server.applicationhistoryservice.metrics.tee;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetric;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetrics;
import org.apache.hadoop.yarn.api.records.timeline.TimelinePutResponse;
import org.apache.hadoop.yarn.server.applicationhistoryservice.metrics.timeline.PhoenixHBaseAccessor;

public class KafkaWriter implements MetricsStoreWriter {
  private static final Log LOG = LogFactory.getLog(KafkaWriter.class);
  ProducerConfig config;
  Producer<byte[], byte[]> producer;
  public void init(Configuration conf) {
    Properties properties = new Properties();
    //InputStream is = this.getClass().getResourceAsStream("/producer.properties");
    //properties.load(is);
    properties.put("serializer.class", "kafka.serializer.DefaultEncoder");
    properties.put("producer.type", "sync");
    properties.put("metadata.broker.list", "localhost:9092");
    config = new ProducerConfig(properties);
    producer = new Producer<byte[], byte[]>(config);
  }

  // decide on topics, partitions, message key
  // decide on serializer and encoder
  @Override
	public void write(TimelineMetrics metrics, TimelinePutResponse response) {
	  List<TimelineMetric> timelineMetrics = metrics.getMetrics();
	  try{
  	  JAXBContext jc = JAXBContext.newInstance(TimelineMetric.class);
  	  Marshaller m = jc.createMarshaller();
  	  for(TimelineMetric metric: timelineMetrics){
  	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
  	    m.marshal(metric, bos);
  	    producer.send(new KeyedMessage<byte[], byte[]>("AMSApplications", metric.getAppId().getBytes(), bos.toByteArray()));
  	  }
	  } catch(Exception e){
	    LOG.error(e);
	  }
	}
}
