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

package org.ow2.erocci.backend.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.EList;
import org.freedesktop.dbus.UInt16;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.UInt64;
import org.freedesktop.dbus.Variant;
import org.occiware.clouddesigner.occi.AttributeState;
import org.occiware.clouddesigner.occi.Entity;
import org.occiware.clouddesigner.occi.Kind;
import org.occiware.clouddesigner.occi.Mixin;
import org.ow2.erocci.backend.Quad;

/**
 * Utilitary class for OCCI backend implem.
 * 
 * @author Pierre-Yves Gibello - Linagora
 *
 */
public class Utils {

	private static Logger logger = Logger.getLogger("Utils");
	public static final String REGEX_CONTROL_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";

	// Conventional name ASCII type-code Encoding
	// BYTE y (121) Unsigned 8-bit integer
	// BOOLEAN b (98) Boolean value: 0 is false, 1 is true, any other value
	// allowed by the marshalling format is invalid
	// INT16 n (110) Signed (two's complement) 16-bit integer
	// UINT16 q (113) Unsigned 16-bit integer
	// INT32 i (105) Signed (two's complement) 32-bit integer
	// UINT32 u (117) Unsigned 32-bit integer
	// INT64 x (120) Signed (two's complement) 64-bit integer (mnemonic: x and t
	// are the first characters in "sixty" not already used for something more
	// common)
	// UINT64 t (116) Unsigned 64-bit integer
	// DOUBLE d (100) IEEE 754 double-precision floating point
	// UNIX_FD h (104) Unsigned 32-bit integer representing an index into an
	// out-of-band array of file descriptors, transferred via some
	// platform-specific mechanism (mnemonic: h for handle)
	//
	/**
	 * Convert a map <String, Variant> to Map<String, String>. Variant is a
	 * specific type from DBUS.
	 * 
	 * @param vmap
	 * @return a Map<String, String>, the map will be empty if vmap is null.
	 */
	public static Map<String, String> convertVariantMap(Map<String, Variant> vmap) {

		if (vmap == null) {
			vmap = new HashMap<String, Variant>();
		}
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<String, Variant> e : vmap.entrySet()) {
			Variant variant = e.getValue();

			if (variant != null) {
				switch (variant.getSig()) {
				case "ay": // Array of bytes, assume String !
					map.put(e.getKey(), new String((byte[]) variant.getValue(), Charset.forName("UTF-8")));
					break;
				case "b": // boolean (0 for false or 1 for true)
					Boolean bool = (Boolean) variant.getValue();
					map.put(e.getKey(), bool.toString());
					break;
				case "n": // signed integer 16
					Short valSho = (Short) variant.getValue();
					map.put(e.getKey(), valSho.toString());

					break;
				case "q": // unsigned integer 16
					UInt16 uint16Val = (UInt16) variant.getValue();
					map.put(e.getKey(), uint16Val.toString());

					break;
				case "i": // signed integer 32
					Integer valInt = (Integer) variant.getValue();
					map.put(e.getKey(), valInt.toString());

					break;
				case "u": // unsigned integer 32
					// logger.info("Convert unsigned integer 32 to String
					// value");
					UInt32 uint32Val = (UInt32) variant.getValue();
					map.put(e.getKey(), uint32Val.toString());

					break;
				case "x": // signed integer 64
					Long lonVal = (Long) variant.getValue();
					map.put(e.getKey(), lonVal.toString());

					break;
				case "t": // unsigned integer 64
					UInt64 uint64Val = (UInt64) variant.getValue();
					map.put(e.getKey(), uint64Val.toString());

					break;
				case "d": // IEEE 754 double-precision floating point
					Double douVal = (Double) variant.getValue();
					map.put(e.getKey(), douVal.toString());
					break;
				case "s":
					// logger.info("String value on input => variant of type
					// string");
					String valStr = (String) variant.getValue();
					map.put(e.getKey(), valStr);
					break;
				case "h": // Unsigned 32-bit integer representing an index into
							// an out-of-band array of file descriptors,
							// transferred via some platform-specific mechanism
							// (mnemonic: h for handle)
					UInt32 uint32ValHandle = (UInt32) variant.getValue();
					map.put(e.getKey(), uint32ValHandle.toString());

					break;
				default:
					logger.warning("WARNING: trying to convert variant of type " + variant.getSig()
							+ " but this doesnt exist for now, and will be implemented in future");
					// TODO : Report exception.

					break;
				}
			} else {
				// TODO : Report exception.
				// Variant must not be null.
				logger.warning("WARNING: entry variant type is null, check this for key: " + e.getKey());
			}
		}
		return map;
	}

	/**
	 * Convert an OCCI Entity to Quad object, this is for dialog with dbus
	 * protocol.
	 * 
	 * @param entity
	 * @return
	 */
	public static Quad<String, String, List<String>, Map<String, Variant>> convertEntityToQuad(Entity entity) {
		if (entity == null) {
			return null;
		}

		// to cast to String
		Kind kind = entity.getKind();

		// to cast to List<String>
		EList<Mixin> mixins = entity.getMixins();

		// to cast to Map<String, Variant>.
		EList<AttributeState> attributes = entity.getAttributes();

		if (kind == null) {
			// TODO : throw exception.
			logger.warning("No kind on this entity !!!");
			// malformed entity. normally this never happen.
			return null;
		}

		String kindStr = kind.getScheme() + kind.getTerm();
		List<String> mixinsStr = new ArrayList<>();
		for (Mixin mixin : mixins) {
			mixinsStr.add(mixin.getScheme() + mixin.getTerm());
		}
		// logger.info("attributes to Variant DBUS FORMAT : ");
		Map<String, Variant> attribVariant = new HashMap<>();
		for (AttributeState attrState : attributes) {
			// logger.info("Key: " + attrState.getName() + " --< Value: " + attrState.getValue());
			// Warning : dont convert undefined Value, this will cause Erocci
			// bug and terminate this application.
			if (!attrState.getValue().equals("undefined")) {
				attribVariant.put(attrState.getName(), new Variant(attrState.getValue()));
			}
		}
		// logger.info("Id: " + entity.getId());
		// logger.info("Kind: " + kindStr);
		// logger.info("mixinsStr: " + mixinsStr);

		return new Quad<>(entity.getId(), kindStr, mixinsStr, attribVariant);

	}

	public static String createUUID() {
		return UUID.randomUUID().toString();

	}

	/**
	 * Simple copy a stream with a buffer of 1024 bytes into an outputstream.
	 * 
	 * @param in
	 * @param os
	 * @return a String representation of copied bytes, null if outputstream is not a ByteArrayOutputStream.
     * @throws IOException
     * 
	 */
	public static String copyStream(InputStream in, OutputStream os) throws IOException {
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
        os.flush();
        if (os instanceof ByteArrayOutputStream) {
            return new String(((ByteArrayOutputStream) os).toByteArray(), "UTF-8");
        }
        return null;
	}

	/**
	 * Close quietly an inputstream without exception thrown.
	 * 
	 * @param in
	 */
	public static void closeQuietly(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				/* ignore */ }
		}
	}

	/**
	 * Close quietly an outputstream without exception thrown.
	 * 
	 * @param os
	 */
	public static void closeQuietly(OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (IOException e) {
				/* ignore */ }
		}
	}

	/**
	 * Serialize an object to make an MD5 hash after call getMd5Digest Method.
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize(Object obj) throws IOException {
		byte[] byteArray = null;
		ByteArrayOutputStream baos = null;
		ObjectOutputStream out = null;
		try {
			// These objects are closed in the finally.
			baos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(baos);
			out.writeObject(obj);
			byteArray = baos.toByteArray();
		} finally {
			if (out != null) {
				out.close();
			}
		}
		return byteArray;
	}

	/**
	 * Create a MD5 hash.
	 * 
	 * @param bytes
	 *            (array of bytes).
	 * @return
	 */
	public static String getMd5Digest(byte[] bytes) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return "1";
			// throw new RuntimeException("MD5 cryptographic algorithm is not
			// available.", e);
		}
		byte[] messageDigest = md.digest(bytes);
		BigInteger number = new BigInteger(1, messageDigest);
		// prepend a zero to get a "proper" MD5 hash value
		StringBuffer sb = new StringBuffer('0');
		sb.append(number.toString(16));
		return sb.toString();
	}

	/**
	 * Create an eTag (Serial number, serialize an object) for dbus interaction.
	 * 
	 * @param an
	 *            object.
	 * @return an eTag number.
	 */
	public static UInt32 createEtagNumber(Object obj) {
		String eTag = null;
		try {
			eTag = getMd5Digest(serialize(obj));
		} catch (IOException ioe) {
			logger.warning("IOException thrown : " + ioe.getMessage());
			eTag = "1";
		}

		StringBuilder sb = new StringBuilder();
		for (char c : eTag.toCharArray()) {
			sb.append((int) c);
		}
		return new UInt32(sb.toString());

	}

	/**
	 * Serialize a string (entity id for example with an owner)
	 * 
	 * @param id
	 * @param owner
	 * @param version
	 *            (version number, will increment with each update on this
	 *            object).
	 * @return
	 */
	public static UInt32 createEtagNumber(final String id, final String owner, final int version) {
		String eTag = null;
		if (id == null) {
			eTag = "1";
		} else {
			try {
				eTag = getMd5Digest(serialize(id + owner + version));
			} catch (IOException ioe) {
				logger.warning("IOException thrown : " + ioe.getMessage());
				eTag = "1";
			}
		}
		StringBuilder sb = new StringBuilder();

		for (char c : eTag.toCharArray()) {
			sb.append((int) c);
		}
		String result = sb.toString().substring(0, 7);

		return new UInt32(result);
	}

	/**
	 * Check if an UUID is provided on a String or attribute occi.core.id.
	 * 
	 * @param id
	 * @param attr
	 * @return true if provided or false if not provided
	 */
	public static boolean isEntityUUIDProvided(final String id, final Map<String, String> attr) {
		String[] uuids = id.split("/");
		boolean match = false;

		for (String uuid : uuids) {
			if (uuid.matches(REGEX_CONTROL_UUID)) {
				match = true;
				break;
			}
		}
		String occiCoreId = attr.get("occi.core.id");
		if (!match && occiCoreId != null && !occiCoreId.isEmpty()) {
			String[] spls = { "/", ":" };
			for (String spl : spls) {
				uuids = occiCoreId.split(spl);
				for (String uuid : uuids) {
					if (uuid.matches(REGEX_CONTROL_UUID)) {
						match = true;
						break;
					}
				}
				if (match) {
					break;
				}
			}

		}

		return match;
	}

	/**
	 * Search for UUID on a String or attribute occi.core.id.
	 * 
	 * @param id
	 * @param attr
	 * @return the UUID provided may return null if uuid not found.
	 */
	public static String getUUIDFromId(final String id, final Map<String, String> attr) {
		String[] uuids = id.split("/");
		String uuidToReturn = null;
		boolean match = false;

		for (String uuid : uuids) {
			if (uuid.matches(REGEX_CONTROL_UUID)) {
				uuidToReturn = uuid;
				break;
			}
		}
		String occiCoreId = attr.get("occi.core.id");
		if (!match && occiCoreId != null && !occiCoreId.isEmpty()) {
			String[] spls = { "/", ":" };
			for (String spl : spls) {
				uuids = occiCoreId.split(spl);
				for (String uuid : uuids) {
					if (uuid.matches(REGEX_CONTROL_UUID)) {
						uuidToReturn = uuid;
						break;
					}
				}
				if (match) {
					break;
				}
			}

		}

		return uuidToReturn;
	}

	/**
	 * Return a relative path from an full entityId with uuid provided.
	 * 
	 * @param id
	 * @return
	 */
	public static String getRelativePathFromId(final String id, final String uuid) {

		String relativePathPart = "";

		relativePathPart = id.replace(uuid, "");
		if (relativePathPart.endsWith("/")) {
			relativePathPart = relativePathPart.substring(0, relativePathPart.length() - 1);
		}

		return relativePathPart;
	}
    
    /**
     * Helper for converting action attributes parameters in array.
     *
     * @param actionAttributes
     * @return parameters for an action null if none.
     */
    public static String[] getActionParametersArray(Map<String, String> actionAttributes) {
        String[] actionParameters = null;
        if (actionAttributes != null && !actionAttributes.isEmpty()) {
            actionParameters = new String[actionAttributes.size()];
            String key;
            String value;
            int index = 0;
            for (Map.Entry<String, String> entry : actionAttributes.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                actionParameters[index] = value;
                index++;
            }
        }

        return actionParameters;
    }

	private static int uniqueInt = 1;

	public static synchronized int getUniqueInt() {
		return uniqueInt++;
	}
}
