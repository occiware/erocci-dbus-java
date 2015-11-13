/**
 * Copyright (c) 2015-2016 Linagora
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Entity {

	private String id;
	private String title;
	private int type;
	private String kind;
	private String owner;
	private List<String> mixins;
	private Map<String, String> attributes;
	private List<Entity> linkedFrom = new LinkedList<Entity>();
	
	// ETag for HTTP cache of resource (used for http rendering)
	private int serial;
	
	// For Entities of type RESOURCE only:
	// - The summary attribute
	// - The entities of type LINK this resource owes.
	private String summary;
	private List<Entity> links;
	
	// For entities of type LINK only
	// The entities of type RESOURCE this link points from and to.
	private Entity source;
	private Entity target;

	public Entity(String id, int type, String kind, List<String> mixins, Map<String, String> attributes, String owner, int serial) {
		super();
		this.id = id;
		this.type = type;
		this.kind = kind;
		this.mixins = mixins;
		this.owner = owner;
		this.serial = serial;
		this.attributes = (attributes != null ? attributes : new HashMap<String, String>());
		
		attributes.put(OcciConstants.ATTRIBUTE_ID, id);
		attributes.put(OcciConstants.ATTRIBUTE_KIND, kind);
		
		if(type == OcciConstants.TYPE_RESOURCE) links = new LinkedList<Entity>();
	}
	
	public String getId() {
		return id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getType() {
		return type;
	}

	public String getKind() {
		return kind;
	}

	public List<String> getMixins() {
		return mixins;
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}

	public int getSerial() {
		return serial;
	}
	
	public void incrementSerial() {
		serial += 1;
	}

	public List<Entity> getLinks() {
		return links;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		attributes.put(OcciConstants.ATTRIBUTE_TITLE, title);
	}
	
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
		attributes.put(OcciConstants.ATTRIBUTE_SUMMARY, summary);
	}

	public Entity getSource() {
		return source;
	}
	
	public void setSource(Entity source) {
		if(type == OcciConstants.TYPE_LINK) {
			this.source = source;
			attributes.put(OcciConstants.ATTRIBUTE_SOURCE, source.getId());
		}
	}
	
	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		if(type == OcciConstants.TYPE_LINK) {
			this.target = target;
			target.getLinkedFrom().add(this);
			attributes.put(OcciConstants.ATTRIBUTE_TARGET, target.getId());
		}
	}

	public List<Entity> getLinkedFrom() {
		return linkedFrom;
	}
}
