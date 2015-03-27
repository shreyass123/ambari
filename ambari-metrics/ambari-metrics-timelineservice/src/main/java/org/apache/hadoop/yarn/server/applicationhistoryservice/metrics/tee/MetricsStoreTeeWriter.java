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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.metrics2.sink.timeline.TimelineMetrics;
import org.apache.hadoop.yarn.api.records.timeline.TimelinePutResponse;
import org.apache.hadoop.yarn.server.applicationhistoryservice.metrics.timeline.TimelineMetricConfiguration;

public class MetricsStoreTeeWriter {
  private List<MetricsStoreWriter> writerList = Collections.synchronizedList(new LinkedList<MetricsStoreWriter>());
  
  static final Log LOG = LogFactory.getLog(MetricsStoreTeeWriter.class);

  public MetricsStoreTeeWriter(Configuration metricsConf){
    if(!metricsConf.getBoolean(TimelineMetricConfiguration.METRICS_STORE_TEE_ENABLE, false)){
      return;
    }    
    String writersProp = metricsConf.get(TimelineMetricConfiguration.METRICS_STORE_TEE_WRITERS, "");
    if(writersProp == null || "".equals(writersProp)){
      return;
    }
    String[] writers = writersProp.split(",");
    if(writers.length == 0){
      return;
    }    
    for(String writer:writers){
      String writerClass = writer.trim();
      try{
        Class<?> clazz = Class.forName(writerClass);
        Object instance = clazz.newInstance();
        if(!(instance instanceof MetricsStoreWriter)){
          LOG.warn("Skipping invalid writer: " + writerClass);
          continue;
        }
        MetricsStoreWriter msw = (MetricsStoreWriter) instance;
        msw.init(metricsConf);
        writerList.add(msw);        
      } catch(Exception e){
        LOG.warn("Error instantiating writer: " + writer);
        LOG.warn(ExceptionUtils.getStackTrace(e));
      }      
    }    
	}

  public void writeMetrics(TimelineMetrics metrics, TimelinePutResponse response) {    
    for(Iterator<MetricsStoreWriter> iter = writerList.iterator(); iter.hasNext();){
      MetricsStoreWriter writer = iter.next();
      writer.write(metrics, response);
    }
  }

}
