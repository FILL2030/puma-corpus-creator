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

import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;

import java.util.ArrayList;
import java.util.List;

public class PumaFileUtil {

	public static boolean hasFilesOfType(DocumentVersion documentVersion, PumaFileType fileType) {
		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getDocumentType().equals(fileType)) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasFilesOfMimeType(DocumentVersion documentVersion, String mimeType) {
		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getMimeType().equals(mimeType)) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasFilesOfType(DocumentVersion documentVersion, PumaFileType fileType, String mimeType) {
		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getDocumentType().equals(fileType) && pumaFile.getMimeType().equals(mimeType)) {
				return true;
			}
		}

		return false;
	}

	public static List<PumaFile> getFilesOfType(DocumentVersion documentVersion, PumaFileType fileType) {
		List<PumaFile> files = new ArrayList<>();

		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getDocumentType().equals(fileType)) {
				files.add(pumaFile);
			}
		}

		return files;
	}

	public static List<PumaFile> getFilesOfType(DocumentVersion documentVersion, String mimeType) {
		List<PumaFile> files = new ArrayList<>();

		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getMimeType().equals(mimeType)) {
				files.add(pumaFile);
			}
		}

		return files;
	}

	public static List<PumaFile> getFilesOfType(DocumentVersion documentVersion, PumaFileType fileType, String mimeType) {
		List<PumaFile> files = new ArrayList<>();

		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getDocumentType().equals(fileType) && pumaFile.getMimeType().equals(mimeType)) {
				files.add(pumaFile);
			}
		}

		return files;
	}

	public static List<PumaFile> getFilesOfTypes(DocumentVersion documentVersion, List<PumaFileType> fileTypes) {
		List<PumaFile> files = new ArrayList<>();

		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (fileTypes.contains(pumaFile.getDocumentType())) {
				files.add(pumaFile);
			}
		}

		return files;
	}

	public static List<PumaFile> getFilesOfTypes(DocumentVersion documentVersion, List<PumaFileType> fileTypes, String mimeType) {
		List<PumaFile> files = new ArrayList<>();

		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (fileTypes.contains(pumaFile.getDocumentType()) && pumaFile.getMimeType().equals(mimeType)) {
				files.add(pumaFile);
			}
		}

		return files;
	}

	public static PumaFile getFirstFileOfType(DocumentVersion documentVersion, PumaFileType fileType) {
		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getDocumentType().equals(fileType)) {
				return pumaFile;
			}
		}

		return null;
	}

	public static PumaFile getFirstFileOfType(DocumentVersion documentVersion, PumaFileType fileType, String mimeType) {
		for (PumaFile pumaFile : documentVersion.getFiles()) {
			if (pumaFile.getDocumentType().equals(fileType) && pumaFile.getMimeType().equals(mimeType)) {
				return pumaFile;
			}
		}

		return null;
	}
}
