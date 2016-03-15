/**
 * Copyright (c) 2015-2017 Inria - Linagora
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

package org.ow2.erocci.model;

/**
 * Constants for OCCI model.
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class OcciConstants {

	public static final byte TYPE_UNDEFINED = 0;
	public static final byte TYPE_CAPABILITIES = 1;
	public static final byte TYPE_RESOURCE = 2;
	public static final byte TYPE_LINK = 3;
	public static final byte TYPE_BOUNDED_COLL = 4;
	public static final byte TYPE_UNBOUNDED_COLL = 5;
	public static final byte TYPE_MIXIN = 6;
	
	public static String ATTRIBUTE_ID = "occi.core.id";
	public static String ATTRIBUTE_TITLE = "occi.core.title";
	public static String ATTRIBUTE_SOURCE = "occi.core.source";
	public static String ATTRIBUTE_TARGET = "occi.core.target";
	public static String ATTRIBUTE_SUMMARY = "occi.core.summary";
	public static String ATTRIBUTE_KIND = "kind";
	public static String ATTRIBUTE_MIXINS = "mixins";
	public static String ATTRIBUTE_LINKS = "links";
}
