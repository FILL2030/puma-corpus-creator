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

/**
 * Created by letreguilly on 26/07/17.
 */

import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.entities.BaseFile;
import eu.ill.puma.core.domain.document.enumeration.BaseFileType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.util.PumaFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by letreguilly on 25/07/17.
 */
public class PdfBoxAnalyser {

	private static final Logger log = LoggerFactory.getLogger(PdfBoxAnalyser.class);

	public PdfBoxAnalyser() {
	}

	protected AnalyserResponse doAnalyse(DocumentVersion document) {
		BaseDocument baseDocument = new BaseDocument();

		AnalyserResponse response = new AnalyserResponse();
		response.setBaseDocument(baseDocument);

		for (PumaFile pumaFile : PumaFileUtil.getFilesOfType(document, "application/pdf")) {
			try {
				this.extractFullText(baseDocument, document.getId(), pumaFile);
				this.setPageNumber(baseDocument, pumaFile);
			} catch (IOException ex) {
				log.error("Pdf to text io error on file : " + pumaFile.getFilePath(), ex);

				response.setSuccessful(false);
				response.setMessage("Pdf to text io error on file : " + pumaFile.getFilePath() + " : " + ex.getMessage());

			} catch (Exception ex) {
				log.error("Caught exception : " + ex.getMessage(), ex);

				response.setSuccessful(false);
				response.setMessage("Caught exception : " + ex.getMessage());
			}
		}

		return response;
	}

	private void extractFullText(BaseDocument baseDocument, Long documentId, PumaFile pumaFile) throws IOException {
		//get imput stream
		InputStream is = new ByteArrayInputStream(pumaFile.getData());

		//extract text
		String text = this.extractText(is);

		String name = null;
		if (pumaFile.getName() != null && pumaFile.getName().contains(".")) {
			name = pumaFile.getName().substring(0, pumaFile.getName().lastIndexOf('.'));
		} else if (pumaFile.getName() == null) {
			name = "extracted";
		} else {
			name = pumaFile.getName();
		}

		//create baseFile
		BaseFile baseFile = new BaseFile();
		baseFile.setBase64Encoded(false);
		baseFile.setData(text);
		baseFile.setMimeType("text/plain");
		baseFile.setName(getFilePrefix() + "-" + name + ".txt");
		baseFile.setType(BaseFileType.EXTRACTED_FULL_TEXT);
		baseFile.setOriginUrl(getFilePrefix() + "-" + UUID.randomUUID().toString() + "-" + name + ".txt");
		baseFile.setMd5(DigestUtils.md5Hex(text));
		baseDocument.getFiles().add(baseFile);
	}

	private void setPageNumber(BaseDocument baseDocument, PumaFile pumaFile) throws IOException {
		//get imput stream
		InputStream is = new ByteArrayInputStream(pumaFile.getData());

		//create baseFile
		BaseFile baseFile = new BaseFile();
		baseFile.setPumaId(pumaFile.getId());
		baseDocument.getFiles().add(baseFile);
	}

	/**
	 * extract text from pdf
	 */
	private String extractText(InputStream is) throws IOException {
		RandomAccessBuffer randomAccessBuffer = new RandomAccessBuffer(is);
		PDFParser parser = new PDFParser(randomAccessBuffer);
		parser.parse();

		COSDocument cosDoc = parser.getDocument();
		PDDocument pdDoc = new PDDocument(cosDoc);

		PDFTextStripper pdfStripper = new PDFTextStripper();
		pdfStripper.setStartPage(1);
		pdfStripper.setEndPage(500000);
		String parsedText = pdfStripper.getText(pdDoc);

		randomAccessBuffer.close();
		pdDoc.close();
		cosDoc.close();

		return parsedText;
	}

	private String getFilePrefix() {
		return "pdfbox-fulltext";
	}

}
