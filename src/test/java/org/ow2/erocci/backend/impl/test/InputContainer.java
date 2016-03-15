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
package org.ow2.erocci.backend.impl.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.Variant;

/**
 * Class container for building an infra test.
 * @author Christophe Gourdin - Inria
 *
 */
public class InputContainer {
	
	private String id;
	
	private String kind;
	
	private List<String> mixins;
	
	private Map<String, Variant> attributes;
	
	private String owner;
	
	// For links.
	private String resSrc;
	private String resTarget;
	
	// For listing with filters.
	private Map<String, Variant> filters;
	
	public InputContainer() {
		attributes = new HashMap<>();
		filters = new HashMap<>();
	}
	
	public InputContainer(String id, String kind, List<String> mixins, Map<String, Variant> attributes, String owner,
			String resSrc, String resTarget) {
		super();
		this.id = id;
		this.kind = kind;
		this.mixins = mixins;
		this.attributes = attributes;
		this.owner = owner;
		this.resSrc = resSrc;
		this.resTarget = resTarget;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public List<String> getMixins() {
		return mixins;
	}

	public void setMixins(List<String> mixins) {
		this.mixins = mixins;
	}

	public Map<String, Variant> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Variant> attributes) {
		this.attributes = attributes;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getResSrc() {
		return resSrc;
	}

	public void setResSrc(String resSrc) {
		this.resSrc = resSrc;
	}

	public String getResTarget() {
		return resTarget;
	}

	public void setResTarget(String resTarget) {
		this.resTarget = resTarget;
	}

	public Map<String, Variant> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Variant> filters) {
		this.filters = filters;
	}
 	
	
	
	
}
