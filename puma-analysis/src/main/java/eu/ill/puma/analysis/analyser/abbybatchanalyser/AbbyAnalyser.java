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

import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.annotation.Analyser;
import eu.ill.puma.analysis.exception.AnalysisException;
import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseFile;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Analyser(name = "abby", enabled = true, maxInstances = 8, limitMaxInstanceToPhysicalCpuCore = false, produces = {EntityType.FULL_TEXT, EntityType.EXTRACTED_IMAGE})
public class AbbyAnalyser extends DocumentAnalyser {

	private static final Logger log = LoggerFactory.getLogger(AbbyAnalyser.class);

	private static final Long maxRunTime = 600 * 1000L;

	private String abbyBatchPath = "";
	private String abbyInputPath = "";
	private String abbyOutputPath = "";

	private PdfBoxAnalyser pdfBoxAnalyser = new PdfBoxAnalyser();

	public AbbyAnalyser(int instanceIndex, Map<String, String> properties) {
		super(instanceIndex, properties);
		this.setupPath();
	}

	public AbbyAnalyser(int instanceIndex, Map<String, String> properties, Analyser analyser) {
		super(instanceIndex, properties, analyser);
		this.setupPath();
	}

	private void setupPath() {
		//batch
		this.abbyBatchPath = properties.get("abbyBatchPath");
		log.info("Using Abby batch path : " + abbyBatchPath);

		//input
		this.abbyInputPath = properties.get("abbyInputPath");
		log.info("Using Abby input path : " + abbyBatchPath);

		//output
		this.abbyOutputPath = properties.get("abbyOutputPath");
		log.info("Using Abby output path : " + abbyBatchPath);
	}


	@Override
	protected synchronized AnalyserResponse doAnalyse(DocumentVersion documentVersion) throws AnalysisException {

		//try batch analyse
		AnalyserResponse response = this.batchAnalyse(documentVersion);

		//otherwise hot analyse
		if (response.getBaseDocument().getFiles().size() == 0) {
			log.info("No text/images files from abby batch folder: Launching abby hot analysis for document " + documentVersion.getId());
			response = this.hotAnalyse(documentVersion);

		} else {
			log.info("Got text/images files from abby batch folder for document " + documentVersion.getId());
		}

//		if (!response.isSuccessful()) {
//			log.info("Fulltext generation with Abbyy failed: trying with PdfBox for document " + documentVersion.getId());
//			AnalyserResponse pdfBoxResponse = pdfBoxAnalyser.doAnalyse(documentVersion);
//			if (pdfBoxResponse.isSuccessful()) {
//				log.info("Fulltext generation with PdfBox succeeded for document " + documentVersion.getId());
//				response = pdfBoxResponse;
//
//			} else {
//				log.info("Fulltext generation with PdfBox failed for document " + documentVersion.getId());
//			}
//		}

		return response;
	}


	private AnalyserResponse batchAnalyse(DocumentVersion documentVersion) throws AnalysisException {
		//setup response
		BaseDocument baseDocument = new BaseDocument();
		AnalyserResponse response = new AnalyserResponse();
		response.setBaseDocument(baseDocument);

		//get file to analyse
		List<PumaFile> documentPfdList = documentVersion.getFiles().stream()
				.filter(file -> file.getMimeType().equals("application/pdf"))
				.collect(Collectors.toList());

		//analyse file
		documentPfdList.forEach(pumaFile -> {

			//prepare analysis
			String path = pumaFile.getFilePath();
			String name = path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf('.'));

			//run analysis
			try {
				List<BaseFile> baseFileList = AbbyUtils.analyseFromName(name, this.abbyBatchPath, this.getFilePrefix(), "html");
				baseDocument.getFiles().addAll(baseFileList);

			} catch (IOException e) {
				log.error("Failed to get abby analysis results for document " + documentVersion.getId() + " : " + e.getMessage());
				response.setSuccessful(false);
				response.setMessage("Abby analysis error for document : " + e.getMessage());
			}
		});

		//return
		return response;
	}

	private AnalyserResponse hotAnalyse(DocumentVersion documentVersion) throws AnalysisException {

		//setup response
		BaseDocument baseDocument = new BaseDocument();
		AnalyserResponse response = new AnalyserResponse();
		response.setBaseDocument(baseDocument);

		//get file to analyse
		List<PumaFile> documentPfdList = documentVersion.getFiles().stream()
				.filter(file -> file.getMimeType().equals("application/pdf"))
				.collect(Collectors.toList());

		//setup io
		File abbyInputDir = new File(this.abbyInputPath);
		File abbyOutputDir = new File(this.abbyOutputPath);
		Long startTime = System.currentTimeMillis();

		//start analysis
		documentPfdList.forEach(pumaFile -> {
			String pdfName = FilenameUtils.getName(FilenameUtils.removeExtension(pumaFile.getFilePath()));
			File pdfInputFile = new File(abbyInputDir.getAbsolutePath() + File.separator + pdfName + ".pdf");

			try {
				//copy file to abby input folder

				FileUtils.writeByteArrayToFile(pdfInputFile, pumaFile.getData(), false);
			} catch (IOException e) {
				log.error("Failed to get abby analysis results for document " + documentVersion.getId() + " : " + e.getMessage());
				response.setSuccessful(false);
				response.setMessage("Abby analysis error for document : " + e.getMessage());
			}

		});

		//wait and parse result
		documentPfdList.forEach(pumaFile -> {
			try {
				String pdfName = FilenameUtils.getName(FilenameUtils.removeExtension(pumaFile.getFilePath()));
				File pdfInputFile = new File(abbyInputDir.getAbsolutePath() + File.separator + pdfName + ".pdf");

				//setup filter to retrieve abby result
				FilenameFilter filenameFilter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if (name.equals(pdfName + ".html") || name.equals(pdfName + ".htm")) {
							return true;
						} else {
							return false;
						}
					}
				};

				//wait
				while (abbyOutputDir.listFiles(filenameFilter).length == 0 && System.currentTimeMillis() < startTime + maxRunTime) {
					Thread.sleep(1000);
				}

				//parse response
				if (System.currentTimeMillis() < startTime + maxRunTime) {
					List<BaseFile> baseFileList = AbbyUtils.analyseFromName(pdfName, this.abbyOutputPath, this.getFilePrefix(), "htm");
					baseDocument.getFiles().addAll(baseFileList);

					log.info("Got " + baseFileList.size() + " files from abby hot analysis for document " + documentVersion.getId());

					//delete origin file
					if (baseFileList.size() > 0) {
						FileUtils.forceDelete(pdfInputFile);
					}
				} else {
					log.error("Failed to get abby analysis results for document " + documentVersion.getId() + " : timeout");
					response.setSuccessful(false);
					response.setMessage("Abby analysis error for document, abby timeout");
				}

			} catch (IOException | InterruptedException e) {
				log.error("Failed to get abby analysis results for document " + documentVersion.getId() + " : " + e.getMessage());
				response.setSuccessful(false);
				response.setMessage("Abby analysis error for document : " + e.getMessage());
			}
		});

		//return
		return response;
	}


	@Override
	protected boolean prepareAnalyser() {
		return true;
	}

	@Override
	public void destroyAnalyser() {

	}

	@Override
	public String getFilePrefix() {
		return "abby-";
	}
}
