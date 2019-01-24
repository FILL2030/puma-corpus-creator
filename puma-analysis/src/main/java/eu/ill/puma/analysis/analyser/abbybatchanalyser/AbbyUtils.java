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
package eu.ill.puma.analysis.analyser.abbybatchanalyser;

import eu.ill.puma.core.domain.document.MetadataConfidence;
import eu.ill.puma.core.domain.document.entities.BaseFile;
import eu.ill.puma.core.domain.document.enumeration.BaseFileType;
import eu.ill.puma.core.utils.FileTools;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AbbyUtils {
	public static List<BaseFile> analyseFromName(String name, String fileRoot, String filePrefix, String htmlExtension) throws IOException {
		List<BaseFile> baseFileList = new ArrayList();

		//setup path
		String htmlFilePath = fileRoot + name + "." + htmlExtension;
		String folderpath = fileRoot + name + "_files";

		//analyse
		baseFileList.add(AbbyUtils.analyseHtmlFile(htmlFilePath, filePrefix));
		baseFileList.addAll(AbbyUtils.analyseFolder(folderpath, filePrefix));

		//Set origin url
		for (int i = 0; i < baseFileList.size(); i++) {
			BaseFile baseFile = baseFileList.get(i);
			baseFile.setOriginUrl("abby:" + name + "." + i + ".pdf");
		}

		return baseFileList;
	}

	private static BaseFile analyseHtmlFile(String filePath, String filePrefix) throws IOException {
		BaseFile baseFile = new BaseFile();

		File htmlFile = new File(filePath);

		String html = FileUtils.readFileToString(htmlFile, "UTF-8");
		Document doc = Jsoup.parse(html);
		String text = doc.text();

		baseFile.setBase64Encoded(false);
		baseFile.setData(text);
		baseFile.setMimeType("text/plain");
		baseFile.setName(filePrefix + FilenameUtils.removeExtension(FilenameUtils.getName(filePath)) + ".txt");
		baseFile.setType(BaseFileType.EXTRACTED_FULL_TEXT);
		baseFile.setConfidence(MetadataConfidence.SURE);
		baseFile.setMd5(DigestUtils.md5Hex(text));

		return baseFile;
	}

	private static List<BaseFile> analyseFolder(String folderPath, String filePrefix) throws IOException {
		List<BaseFile> baseFileList = new ArrayList();

		File folder = new File(folderPath);

		if (folder.isDirectory() == false) {
			throw new IOException(folder + "is not a directory");
		}

		File[] files = folder.listFiles();

		for (int i = 0; i < files.length; i++) {

			File file = files[i];

			if (FilenameUtils.getExtension(file.getName()).equals("css") == false) {
				BaseFile baseFile = new BaseFile();

				baseFile.setBase64Encoded(true);
				baseFile.setData(FileTools.encodeFileToBase64Binary(file.getAbsolutePath()));
				baseFile.setMimeType("image/" + FilenameUtils.getExtension(file.getName()));
				baseFile.setName(filePrefix + file.getName());
				baseFile.setType(BaseFileType.EXTRACTED_IMAGE);
				baseFile.setConfidence(MetadataConfidence.SURE);
				baseFile.setMd5(DigestUtils.md5Hex(IOUtils.toByteArray(new FileInputStream(file))));

				baseFileList.add(baseFile);
			}
		}

		return baseFileList;
	}
}
