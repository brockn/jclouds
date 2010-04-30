/**
 *
 * Copyright (C) 2009 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.aws.s3.blobstore.functions;

import java.util.Map;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jclouds.aws.domain.Region;
import org.jclouds.aws.s3.S3Client;
import org.jclouds.aws.s3.domain.BucketMetadata;
import org.jclouds.blobstore.ContainerNotFoundException;
import org.jclouds.blobstore.domain.MutableStorageMetadata;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.MutableStorageMetadataImpl;
import org.jclouds.domain.Location;
import org.jclouds.logging.Logger;

import com.google.common.base.Function;

/**
 * @author Adrian Cole
 */
@Singleton
public class BucketToResourceMetadata implements Function<BucketMetadata, StorageMetadata> {
   private final S3Client client;
   private final Map<String, ? extends Location> locations;

   @Resource
   protected Logger logger = Logger.NULL;

   @Inject
   BucketToResourceMetadata(S3Client client, Map<String, ? extends Location> locations) {
      this.client = client;
      this.locations = locations;
   }

   public StorageMetadata apply(BucketMetadata from) {
      MutableStorageMetadata to = new MutableStorageMetadataImpl();
      to.setName(from.getName());
      to.setType(StorageType.CONTAINER);
      try {
         String region = client.getBucketLocation(from.getName());
         if (region != null) {
            Location location = locations.get(region.toString());
            if (location == null)
               logger.error("could not get location for region %s in %s", region, locations);
            to.setLocation(location);
         } else {
            logger.error("could not get region for %s", from.getName());
         }
      } catch (ContainerNotFoundException e) {
         logger.error(e,
                  "could not get region for %s, as service suggests the bucket doesn't exist", from
                           .getName());
      }
      return to;
   }
}