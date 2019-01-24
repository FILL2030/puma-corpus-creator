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
package eu.ill.puma.persistence.service.document;

import eu.ill.puma.persistence.PumaTest;
import eu.ill.puma.persistence.domain.document.Document;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.DocumentVersionSource;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.DocumentType;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileStatus;
import eu.ill.puma.persistence.util.MD5Checksum;
import eu.ill.puma.persistence.utils.ResourceLoader;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;

public class PumaFileServiceTest extends PumaTest {

	@Autowired
	private PumaFileService pumaFileService;

	@Autowired
	private DocumentVersionService documentVersionService;

	@Test
	public void contextLoads() throws Exception {
		Assert.assertNotNull(this.pumaFileService);
	}

	private PumaFile createFile(String name) {
		PumaFile pumaFile = new PumaFile();
		pumaFile.setName(name);

		return pumaFile;
	}

	private DocumentVersion createAndSaveDocumentVersion() throws Exception {
		Document document = new Document();
		document.setDocumentType(DocumentType.LETTER);

		DocumentVersion documentVersion = new DocumentVersion();
		documentVersion.setDocument(document);

		DocumentVersionSource documentVersionSource = new DocumentVersionSource();
		documentVersionSource.setImporterShortName("TEST");
		documentVersionSource.setSourceId("testId");
		documentVersionSource.setImportDate(new Date());
		documentVersionSource.setDocumentVersion(documentVersion);

		documentVersion.addSource(documentVersionSource);

		documentVersion = documentVersionService.save(documentVersion);

		Assert.assertTrue(documentVersion.getId() != null);
		Assert.assertTrue(documentVersion.getDocument().getId() != null);

		return documentVersion;
	}


	@Test
	public void createAndRetrieve() throws Exception {
		PumaFile pumaFile = this.createFile("abcdef");
		pumaFileService.save(pumaFile);

		Assert.assertNotNull(pumaFile.getId());

		PumaFile result = pumaFileService.getById(pumaFile.getId());

		Assert.assertEquals("abcdef", result.getName());
		Assert.assertTrue(pumaFileService.getAll().size() > 0);
	}

	@Test
	public void testSingleCreation() throws Exception {
		String text1 = "This is a PumaFile";

		PumaFile pumaFile1 = this.createFile(text1);
		pumaFile1 = pumaFileService.save(pumaFile1);

		Assert.assertNotNull(pumaFile1.getId());
		Long pumaFile1Id = pumaFile1.getId();

		PumaFile pumaFile2 = this.createFile(text1);
		pumaFile2 = pumaFileService.save(pumaFile2);

		Assert.assertNotNull(pumaFile2.getId());
		Long pumaFile2Id = pumaFile2.getId();

		Assert.assertEquals(pumaFile1Id, pumaFile2Id);
	}

	@Test
	public void verifyMD5SameForByteDataAndFile() throws Exception {
		byte[] byteArray = ResourceLoader.readByteArray("ill.jpg");

		URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource("ill.jpg");

		String checksum1 = MD5Checksum.getMD5ChecksumForFile(resourceUrl.getFile());
		String checksum2 = MD5Checksum.getMD5Checksum(byteArray);

		Assert.assertEquals(checksum1, checksum2);

	}

	@Test(expected = PumaFileService.PumaFilePersistenceException.class)
	public void verifySaveCreatesExceptionWithoutDocumentVersion() throws Exception {
		PumaFile pumaFile = this.createFile("test");
		byte[] byteArray = ResourceLoader.readByteArray("ill.jpg");
		pumaFile.setData(byteArray);

		this.pumaFileService.save(pumaFile);
	}

	@Test
	public void verifyFileSaved() throws Exception {
		PumaFile pumaFile = this.createFile("test");
		byte[] byteArray = ResourceLoader.readByteArray("ill.jpg");
		pumaFile.setData(byteArray);

		DocumentVersion documentVersion = this.createAndSaveDocumentVersion();

		documentVersion.addFile(pumaFile);
		pumaFile.setDocumentVersion(documentVersion);

		pumaFile = this.pumaFileService.save(pumaFile);

		Assert.assertTrue(pumaFile.getId() != null);
		Assert.assertTrue(pumaFile.getFilePath() != null);
		Assert.assertNotNull(pumaFile.getFileSize());

		// Get the file
		File file = new File(this.pumaFileService.getFilesRoot() + File.separator + pumaFile.getFilePath());
		Assert.assertTrue(file.exists());
	}

	@Test
	public void verifyFilesRetrievedForADocumentVersion() throws Exception {
		DocumentVersion documentVersion = this.createAndSaveDocumentVersion();

		PumaFile pumaFile1 = this.createFile("test");

		documentVersion.addFile(pumaFile1);
		pumaFile1.setDocumentVersion(documentVersion);

		this.pumaFileService.save(pumaFile1);

		List<PumaFile> allFiles = this.pumaFileService.getAllForDocumentVersion(documentVersion);
		Assert.assertEquals(1, allFiles.size());

		PumaFile pumaFile2 = this.createFile("another test");

		documentVersion.addFile(pumaFile2);
		pumaFile2.setDocumentVersion(documentVersion);

		this.pumaFileService.save(pumaFile2);

		allFiles = this.pumaFileService.getAllForDocumentVersion(documentVersion);
		Assert.assertEquals(2, allFiles.size());
	}


	@Test
	public void verifyFileChangesDataChanges() throws Exception {
		DocumentVersion documentVersion = this.createAndSaveDocumentVersion();

		PumaFile pumaFile = this.createFile("test");
		byte[] byteArray = ResourceLoader.readByteArray("ill.jpg");
		pumaFile.setData(byteArray);

		documentVersion.addFile(pumaFile);
		pumaFile.setDocumentVersion(documentVersion);

		pumaFile = this.pumaFileService.save(pumaFile);

		String path1 = pumaFile.getFilePath();
		String md51 = pumaFile.getMd5();

		byteArray = ResourceLoader.readByteArray("ill-logo.jpg");
		pumaFile.setData(byteArray);

		pumaFile = this.pumaFileService.save(pumaFile);

		String path2 = pumaFile.getFilePath();
		String md52 = pumaFile.getMd5();

		Assert.assertEquals(path1, path2);
		Assert.assertNotEquals(md51, md52);

	}

	@Test
	public void verifyRetrievalByStatus() throws Exception {
		PumaFile pumaFile1 = this.createFile("abcdef");
		pumaFile1.setStatus(PumaFileStatus.PENDING);
		pumaFileService.save(pumaFile1);

		PumaFile pumaFile2 = this.createFile("12345");
		pumaFile2.setStatus(PumaFileStatus.SAVED);
		pumaFileService.save(pumaFile2);

		PumaFile pumaFile3 = this.createFile("FDSQGF");
		pumaFile3.setStatus(PumaFileStatus.SAVED);
		pumaFileService.save(pumaFile3);

		List<PumaFile> allFiles = this.pumaFileService.getAll();
		Assert.assertTrue(allFiles.size() == 3);

		List<PumaFile> downloadingFiles = this.pumaFileService.getAllForStatus(PumaFileStatus.PENDING);
		Assert.assertTrue(downloadingFiles.size() == 1);

		List<PumaFile> savedFiles = this.pumaFileService.getAllForStatus(PumaFileStatus.SAVED);
		Assert.assertTrue(savedFiles.size() == 2);
	}

	@Test
	public void verifyFileNameExtensionCreation() throws Exception {
		PumaFile pumaFile1 = this.createFile("abcdef");
		pumaFile1.setMimeType("application/pdf");

		String uniqueName1 = pumaFile1.getUniqueFileName();

		Assert.assertEquals(Integer.toHexString(pumaFile1.hashCode()) + "-abcdef.pdf", uniqueName1);

		PumaFile pumaFile2 = this.createFile("abcdef.pdf");
		pumaFile2.setMimeType("application/pdf");

		String uniqueName2 = pumaFile2.getUniqueFileName();

		Assert.assertEquals(Integer.toHexString(pumaFile2.hashCode()) + "-abcdef.pdf", uniqueName2);
	}



}