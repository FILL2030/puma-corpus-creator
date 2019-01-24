/*
 * Copyright 2019 Institut Laue–Langevin
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
package eu.ill.puma.persistence.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checksum {

	private static final Logger log = LoggerFactory.getLogger(MD5Checksum.class);

	static final String HEXES = "0123456789ABCDEF";

	public static String getMD5ChecksumForFile(String filename) throws Exception {
		byte[] md5ByteArray = createChecksumFromFile(filename);

		return getHex(md5ByteArray);

	}

	public static String getMD5Checksum(String stringData) {
		byte[] md5ByteArray = createChecksumFromString(stringData);

		return getHex(md5ByteArray);

	}

	public static String getMD5Checksum(byte[] byteArray) {
		byte[] md5ByteArray = createChecksumFromByteData(byteArray);

		return getHex(md5ByteArray);

	}

	private static byte[] createChecksumFromFile(String filename) throws Exception {
		InputStream fileInputStream =  new FileInputStream(filename);

		byte[] buffer = new byte[1024];
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fileInputStream.read(buffer);
			if (numRead > 0) {
				messageDigest.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fileInputStream.close();
		return messageDigest.digest();
	}

	private static byte[] createChecksumFromString(String text) {
		byte[] byteArray = text.getBytes();

		return createChecksumFromByteData(byteArray);
	}

	private static byte[] createChecksumFromByteData(byte[] byteArray) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			return messageDigest.digest(byteArray);

		} catch (NoSuchAlgorithmException e) {
			log.error("Could not file MD5 checksum in MessageDigest");
		}

		return null;
	}

	private static String getHex( byte [] raw ) {
//		String result = "";
//
//		for (int i=0; i < b.length; i++) {
//			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
//		}
//		return result;

		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder( 2 * raw.length );
		for ( final byte b : raw ) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4))
					.append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}
}