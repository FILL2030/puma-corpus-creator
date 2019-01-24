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
package eu.ill.puma.importermanager.resolver.domain;

import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResolverResponseUrl {

	private static final Logger log = LoggerFactory.getLogger(ResolverResponseUrl.class);

	private String url;
	private ResolverResponseUrlFileType fileType = ResolverResponseUrlFileType.PUBLICATION;

	public ResolverResponseUrl() {

	}

	public ResolverResponseUrl(String url) {
		this.url = url;
		this.fileType = ResolverResponseUrlFileType.PUBLICATION;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ResolverResponseUrlFileType getFileType() {
		return fileType;
	}

	public void setFileType(ResolverResponseUrlFileType fileType) {
		this.fileType = fileType;
	}

	public PumaFile convertToPumaFile() {
		PumaFile pumaFile = new PumaFile();
		pumaFile.setOriginUrl(this.url);
		pumaFile.setDocumentType(this.convertFileType(this.fileType));

		return pumaFile;
	}

	public PumaFileType convertFileType(ResolverResponseUrlFileType resolverResponseUrlFileType) {
		PumaFileType pumaFileType = PumaFileType.valueOf(resolverResponseUrlFileType.toString());

		if (pumaFileType == null) {
			pumaFileType = PumaFileType.UNKNOWN;
			log.warn("Could not convert file type " + resolverResponseUrlFileType);
		}

		return pumaFileType;
	}
}
