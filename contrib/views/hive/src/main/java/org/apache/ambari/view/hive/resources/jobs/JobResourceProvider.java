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

package org.apache.ambari.view.hive.resources.jobs;

import com.google.inject.Inject;
import org.apache.ambari.view.*;
import org.apache.ambari.view.hive.persistence.utils.ItemNotFound;
import org.apache.ambari.view.hive.persistence.utils.OnlyOwnersFilteringStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Resource provider for job
 */
public class JobResourceProvider implements ResourceProvider<Job> {
  @Inject
  ViewContext context;

  protected JobResourceManager resourceManager = null;
  protected final static Logger LOG =
      LoggerFactory.getLogger(JobResourceProvider.class);

  protected synchronized JobResourceManager getResourceManager() {
    if (resourceManager == null) {
      resourceManager = new JobResourceManager(context);
    }
    return resourceManager;
  }

  @Override
  public Job getResource(String resourceId, Set<String> properties) throws SystemException, NoSuchResourceException, UnsupportedPropertyException {
    try {
      return getResourceManager().read(Integer.valueOf(resourceId));
    } catch (ItemNotFound itemNotFound) {
      throw new NoSuchResourceException(resourceId);
    }
  }

  @Override
  public Set<Job> getResources(ReadRequest readRequest) throws SystemException, NoSuchResourceException, UnsupportedPropertyException {
    return new HashSet<Job>(getResourceManager().readAll(
        new OnlyOwnersFilteringStrategy(this.context.getUsername())));
  }

  @Override
  public void createResource(String s, Map<String, Object> stringObjectMap) throws SystemException, ResourceAlreadyExistsException, NoSuchResourceException, UnsupportedPropertyException {
    Job item = null;
    try {
      item = new JobImpl(stringObjectMap);
    } catch (InvocationTargetException e) {
      throw new SystemException("error on creating resource", e);
    } catch (IllegalAccessException e) {
      throw new SystemException("error on creating resource", e);
    }
    getResourceManager().create(item);
    JobController jobController = JobControllerFactory.getInstance(context).createControllerForJob(item);
    jobController.submit();
  }

  @Override
  public boolean updateResource(String resourceId, Map<String, Object> stringObjectMap) throws SystemException, NoSuchResourceException, UnsupportedPropertyException {
    Job item = null;
    try {
      item = new JobImpl(stringObjectMap);
    } catch (InvocationTargetException e) {
      throw new SystemException("error on updating resource", e);
    } catch (IllegalAccessException e) {
      throw new SystemException("error on updating resource", e);
    }
    try {
      getResourceManager().update(item, Integer.valueOf(resourceId));
    } catch (ItemNotFound itemNotFound) {
      throw new NoSuchResourceException(resourceId);
    }
    return true;
  }

  @Override
  public boolean deleteResource(String resourceId) throws SystemException, NoSuchResourceException, UnsupportedPropertyException {
    try {
      getResourceManager().delete(Integer.valueOf(resourceId));
    } catch (ItemNotFound itemNotFound) {
      throw new NoSuchResourceException(resourceId);
    }
    return true;
  }
}
