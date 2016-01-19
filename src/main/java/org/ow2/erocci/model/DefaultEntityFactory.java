package org.ow2.erocci.model;

import java.util.List;
import java.util.Map;

public class DefaultEntityFactory implements EntityFactory {

	@Override
	public Entity newEntity(String id, int type, String kind,
			List<String> mixins, Map<String, String> attributes, String owner,
			int serial) {

		return new Entity(id, type, kind, mixins, attributes, owner, serial);
	}

}
