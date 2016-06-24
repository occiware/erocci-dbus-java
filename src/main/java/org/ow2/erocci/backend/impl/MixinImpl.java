/**
 * Copyright (c) 2015-2017 Inria
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ow2.erocci.backend.impl;

import org.occiware.clouddesigner.occi.Mixin;
//import org.ow2.erocci.backend.mixin;
import org.ow2.erocci.model.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of OCCI mixin.
 *
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class MixinImpl { // implements mixin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MixinImpl.class);

//    @Override
//    public boolean isRemote() {
//
//        return false;
//    }
//
//    /**
//     * Register a user mixin
//     *
//     * @param id , user mixin category if
//     * @param location, relative path url part
//     * @param owner
//     *
//     */
//    @Override
//    public void AddMixin(String id, String location, String owner) {
//        LOGGER.info("add user mixin with id: " + id + " --< location : " + location + " --< owner : " + owner);
//
//        if (id == null) {
//            LOGGER.info("No mixin id provided ! Cant add a user mixin tag.");
//            return;
//        }
//        if (location == null) {
//            LOGGER.info("No location provided, cant add a user mixin tag");
//            return;
//        }
//        if (owner == null) {
//            // Defaulting to anonymous.
//            owner = ConfigurationManager.DEFAULT_OWNER;
//        }
//
//        ConfigurationManager.addUserMixinOnConfiguration(id, location, owner);
//
//        Mixin mixin = ConfigurationManager.findUserMixinOnConfigurations(id);
//        
//
//        // Ex : POST /.well-knwown/org/ogf/occi/
//        //      Content-Type: text/occi
//        //		Category: my_tag; scheme="http://example.com/tag"; location="/tag/"
//        // Update Tag collection :
//        //  PUT /tag/
//        //  Content-Type: text/occi
//        //  X-OCCI-Location: /storage/abc, /network/123.
//    }
//
//    /**
//     * Delete user mixin
//     *
//     * @param id, user mixin category id
//     */
//    @Override
//    public void DelMixin(String id) {
//        LOGGER.info("delete mixin with category id : " + id);
//
//        if (id == null) {
//            LOGGER.info("No mixin id provided ! Cant add a user mixin tag.");
//            return;
//        }
//
//        ConfigurationManager.removeUserMixinFromConfiguration(id);
//
//    }

}
