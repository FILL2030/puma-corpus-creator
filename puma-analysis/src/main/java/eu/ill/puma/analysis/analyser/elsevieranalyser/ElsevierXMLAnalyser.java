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
package eu.ill.puma.analysis.analyser.elsevieranalyser;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.annotation.Analyser;
import eu.ill.puma.analysis.exception.AnalysisException;
import eu.ill.puma.core.domain.analysis.AnalyserResponse;
import eu.ill.puma.core.domain.document.BaseDocument;
import eu.ill.puma.core.domain.document.MetadataConfidence;
import eu.ill.puma.core.domain.document.entities.BaseFile;
import eu.ill.puma.core.domain.document.entities.BaseLaboratory;
import eu.ill.puma.core.domain.document.entities.BasePerson;
import eu.ill.puma.core.domain.document.entities.BaseStringEntity;
import eu.ill.puma.core.domain.document.enumeration.BaseFileType;
import eu.ill.puma.core.domain.fulltext.Figure;
import eu.ill.puma.core.domain.fulltext.FullText;
import eu.ill.puma.core.domain.fulltext.Section;
import eu.ill.puma.core.utils.FormattedToFlatFullText;
import eu.ill.puma.persistence.domain.analysis.EntityType;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.domain.document.PumaFile;
import eu.ill.puma.persistence.domain.document.enumeration.PumaFileType;
import eu.ill.puma.persistence.util.PumaFileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.text.Normalizer;
import java.util.*;

@Analyser(name = "elsevierxml", maxInstances = 1, produces = {EntityType.FULL_TEXT})
public class ElsevierXMLAnalyser extends DocumentAnalyser {

	private static final Logger log = LoggerFactory.getLogger(ElsevierXMLAnalyser.class);

	public ElsevierXMLAnalyser(int instanceIndex, Map<String, String> properties) {
		super(instanceIndex, properties);
	}

	public String getFilePrefix() {
		return "elsevier-fulltext";
	}

	private String sanitize(String value) {
		return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", " ").replaceAll("[\\n\\t]", " ").replaceAll(" +", " ");
	}

	@Override
	protected AnalyserResponse doAnalyse(DocumentVersion document) throws AnalysisException {
		// Get XML files
		List<PumaFileType> pumaFileTypeList = new ArrayList();
		pumaFileTypeList.add(PumaFileType.PUBLICATION);
		List<PumaFile> filesToAnalyse = PumaFileUtil.getFilesOfType(document, PumaFileType.PUBLICATION, PumaFile.XML_MIME_TYPE);

		// Create xpath
		XPath xPath = XPathFactory.newInstance().newXPath();

		//response
		BaseDocument baseDocumentWithFullText = new BaseDocument();
		AnalyserResponse response = new AnalyserResponse();
		response.setBaseDocument(baseDocumentWithFullText);

		BaseDocument dummyBaseDocument = new BaseDocument();

		//file loop
		for (PumaFile pumaFile : filesToAnalyse) {
			// Convert binary data to string
			String xmlData = new String(pumaFile.getData());

			try {
				//get document
				Document xml = this.parseXML(xmlData);

				//get root
				Element root = xml.getDocumentElement();

				if (this.isElsevierXml(xPath, root)) {

					//parse document
					this.parseTitle(dummyBaseDocument, xPath, root);
					this.parseAbstract(dummyBaseDocument, xPath, root);
					this.parseKeywords(dummyBaseDocument, xPath, root);
					this.parseReferences(dummyBaseDocument, xPath, root);
					this.parseLaboratoriesAndPersons(dummyBaseDocument, xPath, root);
					this.parseFullText(dummyBaseDocument, xPath, root);

					// Set full text file only in baseDocument to return
					dummyBaseDocument.getFiles().forEach(file -> baseDocumentWithFullText.addFile(file));

				} else {
					log.info("XML file " + document.getId() + " is not from elsevier");
				}

			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
				log.error("Failed to parse XML file document version " + document.getId() + " : " + ex.getMessage(), ex);

				response.setSuccessful(false);
				response.setMessage(ex.getMessage());
			}
		}

		return response;
	}

	@Override
	protected boolean prepareAnalyser() {
		return true;
	}

	@Override
	public void destroyAnalyser() {

	}


	private Document parseXML(String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(xml));
		Document xmlDocument = builder.parse(inputSource);
		xmlDocument.getDocumentElement().normalize();
		return xmlDocument;
	}

	private boolean isElsevierXml(XPath xPath, Node root) throws XPathExpressionException {
		// Research domains
		Node coreData = (Node) xPath.evaluate("/full-text-retrieval-response/coredata", root, XPathConstants.NODE);
		NodeList sectionNodes = (NodeList)xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/body/*[name()='ce:sections']/*[name()='ce:section']", root, XPathConstants.NODESET);
		NodeList bibReferenceNodes = (NodeList)xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/tail/*[name()='ce:bibliography']/*[name()='ce:bibliography-sec']/*[name()='ce:bib-reference']", root, XPathConstants.NODESET);
		NodeList bibReferenceNodes2 = (NodeList)xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/converted-article/tail/*[name()='ce:bibliography']/*[name()='ce:bibliography-sec']/*[name()='ce:bib-reference']", root, XPathConstants.NODESET);

		if (coreData != null && (sectionNodes.getLength() > 0 || bibReferenceNodes.getLength() > 0 || bibReferenceNodes2.getLength() > 0)) {
			return true;
		}

		return false;
	}

	private void parseTitle(BaseDocument baseDocument, XPath xPath, Node root) throws XPathExpressionException {
		Node titleNode = (Node) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/head/*[name()='ce:title']", root, XPathConstants.NODE);
		if (titleNode == null) {
			titleNode = (Node) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/converted-article/head/*[name()='ce:title']", root, XPathConstants.NODE);
		}

		if (titleNode != null) {
			String title = titleNode == null ? "" : titleNode.getTextContent();
			title = sanitize(title);


			BaseStringEntity titleEntity = new BaseStringEntity(title);
			titleEntity.setConfidence(MetadataConfidence.CONFIDENT);

			baseDocument.setTitle(titleEntity);
		}
	}

	private void parseAbstract(BaseDocument baseDocument, XPath xPath, Node root) throws XPathExpressionException {
		Node abstractNode = (Node) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/head/*[name()='ce:abstract']", root, XPathConstants.NODE);
		if (abstractNode == null) {
			abstractNode = (Node) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/converted-article/head/*[name()='ce:abstract']", root, XPathConstants.NODE);
		}

		if (abstractNode != null) {
			String abstractText = abstractNode == null ? "" : abstractNode.getTextContent();
			abstractText = sanitize(abstractText);


			BaseStringEntity abstractEntity = new BaseStringEntity(abstractText);
			abstractEntity.setConfidence(MetadataConfidence.CONFIDENT);

			baseDocument.setAbstract(abstractEntity);
		}
	}

	private void parseKeywords(BaseDocument baseDocument, XPath xPath, Node root) throws XPathExpressionException {
		// Keywords

		NodeList keywords = (NodeList) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/head/*[name()='ce:keywords']/*[name()='ce:keyword']/*[name()='ce:text']", root, XPathConstants.NODESET);
		for (int i = 0; i < keywords.getLength(); i++) {
			String keyword = keywords.item(i).getTextContent();

			if (keyword != null) {
				keyword = sanitize(keyword);

				BaseStringEntity baseKeyword = new BaseStringEntity(keyword);
				baseKeyword.setConfidence(MetadataConfidence.CONFIDENT);
				baseDocument.addKeyword(baseKeyword);
			}
		}
	}

	private void parseLaboratoriesAndPersons(BaseDocument baseDocument, XPath xPath, Node root) throws XPathExpressionException {
		// Laboratories
		Map<String, BaseLaboratory> laboratories = new HashMap<>();
		long laboCounter = 0;

//		NodeList authorGroup = (NodeList) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/head/*[name()='ce:author-group']", root, XPathConstants.NODESET);

		NodeList affiliationNodes = (NodeList) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/head/*[name()='ce:author-group']/*[name()='ce:affiliation']", root, XPathConstants.NODESET);
		for (int i = 0; i < affiliationNodes.getLength(); i++) {
			Node affiliationNode = affiliationNodes.item(i);
			Node idNode = affiliationNode.getAttributes().getNamedItem("id");
			String id = idNode == null ? "" : idNode.getTextContent();
			id = sanitize(id);

			Node addressNode = (Node)xPath.evaluate("*[name()='ce:textfn']", affiliationNode, XPathConstants.NODE);
			if (addressNode != null && !id.equals("")) {
				String address = addressNode.getTextContent();

				if (!address.equals("")) {
					BaseLaboratory laboratory = new BaseLaboratory();
					// set into name as well since it is difficult to know which part of the string is the address and which is the insitute name

					address = sanitize(address);
					laboratory.setName(address);
					laboratory.setAddress(address);
					laboratory.setId(laboCounter);
					laboCounter++;
					laboratory.setConfidence(MetadataConfidence.CONFIDENT);

					laboratories.put(id, laboratory);
					baseDocument.addLaboratory(laboratory);
				}
			}
		}

		NodeList authorNodes = (NodeList) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/head/*[name()='ce:author-group']/*[name()='ce:author']", root, XPathConstants.NODESET);
		for (int i = 0; i < authorNodes.getLength(); i++) {
			Node authorNode = authorNodes.item(i);

			Node givenNameNode = (Node)xPath.evaluate("*[name()='ce:given-name']", authorNode, XPathConstants.NODE);
			Node surnameNode = (Node)xPath.evaluate("*[name()='ce:surname']", authorNode, XPathConstants.NODE);
			Node emailNode = (Node)xPath.evaluate("*[name()='ce:e-address']", authorNode, XPathConstants.NODE);

			String givenName = givenNameNode == null ? "" : givenNameNode.getTextContent();
			String surname = surnameNode == null ? "" : surnameNode.getTextContent();
			String email = emailNode == null ? "" : emailNode.getTextContent();

			givenName = sanitize(givenName);
			surname = sanitize(surname);
			email = sanitize(email);

			if (!givenName.equals("") && !surname.equals("")) {

				List<BaseLaboratory> affiliations = new ArrayList<>();
				NodeList crossRefNodes = (NodeList)xPath.evaluate("*[name()='ce:cross-ref']", authorNode, XPathConstants.NODESET);
				for (int j = 0; j < crossRefNodes.getLength(); j++) {
					Node crossRefNode = crossRefNodes.item(j);
					Node refIdNode = crossRefNode.getAttributes().getNamedItem("refid");
					String refId = refIdNode == null ? "" : refIdNode.getTextContent();

					refId = sanitize(refId);

					// add laboratory reference
					BaseLaboratory laboratory = laboratories.get(refId);
					if (laboratory != null) {
						affiliations.add(laboratory);
					}
				}

				// Add persons to document
				if (affiliations.size() > 0) {
					// Add person to document with affiliations
					for (BaseLaboratory laboratory : affiliations) {
						baseDocument.addPerson(this.createPerson(givenName, surname, email, laboratory));
					}

				} else {
					// Add person to document without affilition
					baseDocument.addPerson(this.createPerson(givenName, surname, email));
				}
			}
		}
	}

	private BasePerson createPerson(String givenName, String surname, String email) {
		return createPerson(givenName, surname, email, null);
	}

	private BasePerson createPerson(String givenName, String surname, String email, BaseLaboratory laboratory) {
		BasePerson basePerson = new BasePerson();
		basePerson.setFirstName(givenName);
		basePerson.setLastName(surname);
		basePerson.setEmail(email);
		basePerson.setConfidence(MetadataConfidence.CONFIDENT);

		if (laboratory != null) {
			basePerson.setLaboratoryId(laboratory.getId());
		}

		return basePerson;
	}

	private void parseFullText(BaseDocument baseDocument, XPath xPath, Node root) throws XPathExpressionException, JsonProcessingException {
		FullText fullText = new FullText();

		// Title
		if (baseDocument.getTitle() != null) {
			fullText.setTitle(baseDocument.getTitle().getValue());
		}

		// Abstract
		if (baseDocument.getAbstract() != null) {
			fullText.setAbstract(baseDocument.getAbstract().getValue());
		}

		// Get figures
		NodeList figureNodes = (NodeList)xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/*[name()='ce:floats']/*[name()='ce:figure']", root, XPathConstants.NODESET);
		int numberOfFigures = figureNodes.getLength();
		for (int i = 0; i < numberOfFigures; i++) {
			Node figureNode = figureNodes.item(i);

			Node figureIdNode = figureNode.getAttributes().getNamedItem("id");
			String figureId = figureIdNode == null ? "" : figureIdNode.getTextContent();
			figureId = sanitize(figureId);

			Node figureTitleNode = (Node)xPath.evaluate("*[name()='ce:label']", figureNode, XPathConstants.NODE);
			String figureTitle = figureTitleNode == null ? "" : figureTitleNode.getTextContent();
			figureTitle = sanitize(figureTitle);

			NodeList figureParagraphNodes = (NodeList)xPath.evaluate("*[name()='ce:caption']/*[name()='ce:simple-para']", figureNode, XPathConstants.NODESET);
			int numberOfParagraphs = figureParagraphNodes.getLength();
			String paragraphText = "";
			for (int ip = 0; ip < numberOfParagraphs; ip++) {
				Node figureParagraphNode = figureParagraphNodes.item(ip);
				String paragraph = figureParagraphNode.getTextContent();
				paragraphText += sanitize(paragraph);
			}

			Figure figure = new Figure(figureId);
			figure.setTitle(figureTitle);
			figure.setDescription(paragraphText);

			fullText.addFigure(figure);
		}

		// Get sections
		NodeList sectionNodes = (NodeList)xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/body/*[name()='ce:sections']/*[name()='ce:section']", root, XPathConstants.NODESET);
		int numberOfSections = sectionNodes.getLength();
		for (int i = 0; i < numberOfSections; i++) {
			Node sectionNode = sectionNodes.item(i);
			this.parseSection(xPath, sectionNode, fullText);
		}

		// Acknowledgement
		Node ackNode = (Node)xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/body/*[name()='ce:acknowledgment']", root, XPathConstants.NODE);
		if (ackNode != null) {
			this.parseSection(xPath, ackNode, fullText);
		}

		// Add references
		List<BaseStringEntity> references = baseDocument.getReferences();
		for (BaseStringEntity stringEntity : references) {
			fullText.addReference(stringEntity.getValue());
		}

		// Add unformatted fulltext
		String flatFullText = FormattedToFlatFullText.unformat(fullText);
		BaseFile fullTextFile = new BaseFile();
		fullTextFile.setBase64Encoded(false);
		fullTextFile.setMimeType("text/plain");
		fullTextFile.setType(BaseFileType.EXTRACTED_FULL_TEXT);
		fullTextFile.setName(this.getFilePrefix() + ".txt");
		fullTextFile.setOriginUrl(this.getFilePrefix() + "-" + UUID.randomUUID().toString() + ".txt");
		fullTextFile.setData(flatFullText);
		fullTextFile.setMd5(DigestUtils.md5Hex(flatFullText));
		baseDocument.addFile(fullTextFile);

	}

	private void parseSection(XPath xPath, Node sectionNode, FullText fullText) throws XPathExpressionException {
		Node sectionIdNode = sectionNode.getAttributes().getNamedItem("id");
		String sectionId = sectionIdNode == null ? "" : sectionIdNode.getTextContent();
		sectionId = sanitize(sectionId);

		Node sectionTitleNode = (Node)xPath.evaluate("*[name()='ce:section-title']", sectionNode, XPathConstants.NODE);
		String sectionTitle = sectionTitleNode == null ? "" : sectionTitleNode.getTextContent();
		sectionTitle = sanitize(sectionTitle);

		NodeList sectionParagraphNodes = (NodeList)xPath.evaluate("*[name()='ce:para']", sectionNode, XPathConstants.NODESET);
		int numberOfParagraphs = sectionParagraphNodes.getLength();
		String paragraphText = "";
		for (int ip = 0; ip < numberOfParagraphs; ip++) {
			Node sectionParagraphNode = sectionParagraphNodes.item(ip);
			String paragraph = sectionParagraphNode.getTextContent();
			paragraphText += sanitize(paragraph);
		}

		// Individual cross refs
		NodeList sectionCrossRefNodes = (NodeList)xPath.evaluate("*[name()='ce:para']/*[name()='ce:cross-ref']", sectionNode, XPathConstants.NODESET);
		List<String> figureIds = new ArrayList<>();
		int numberOfCrossRefs = sectionCrossRefNodes.getLength();
		for (int ic = 0; ic < numberOfCrossRefs; ic++) {
			Node sectionCrossRefNode = sectionCrossRefNodes.item(ic);
			Node crossRefIdNode = sectionCrossRefNode.getAttributes().getNamedItem("refid");
			if (crossRefIdNode != null) {
				String crossRefId = crossRefIdNode.getTextContent();
				crossRefId = sanitize(crossRefId);

				if (fullText.getFigureWithId(crossRefId) != null && !figureIds.contains(crossRefId)) {
					figureIds.add(crossRefId);
				}
			}
		}

		// Grouped cross refs
		sectionCrossRefNodes = (NodeList)xPath.evaluate("*[name()='ce:para']/*[name()='ce:cross-refs']", sectionNode, XPathConstants.NODESET);
		numberOfCrossRefs = sectionCrossRefNodes.getLength();
		for (int ic = 0; ic < numberOfCrossRefs; ic++) {
			Node sectionCrossRefNode = sectionCrossRefNodes.item(ic);

			Node crossRefIdsNode = sectionCrossRefNode.getAttributes().getNamedItem("refid");
			if (crossRefIdsNode != null) {
				String crossRefIdsString = crossRefIdsNode.getTextContent();
				crossRefIdsString = sanitize(crossRefIdsString);
				String[] crossRefIds = crossRefIdsString.split(" ");
				for (String crossRefId : crossRefIds) {
					if (fullText.getFigureWithId(crossRefId) != null && !figureIds.contains(crossRefId)) {
						figureIds.add(crossRefId);
					}
				}
			}
		}

		Section section = new Section(sectionId);
		section.setTitle(sectionTitle);
		section.setText(paragraphText);
		section.setFigureIdsList(figureIds);

		fullText.addSection(section);

		// Find subsections
		NodeList subSectionNodes = (NodeList)xPath.evaluate("*[name()='ce:section']", sectionNode, XPathConstants.NODESET);
		int numberOfSubSections = subSectionNodes.getLength();
		for (int i = 0; i < numberOfSubSections; i++) {
			Node subSectionNode = subSectionNodes.item(i);
			this.parseSection(xPath, subSectionNode, fullText);
		}

	}

	private void parseReferences(BaseDocument baseDocument, XPath xPath, Node root) throws XPathExpressionException {

		List<BaseStringEntity> references = baseDocument.getReferences();

		NodeList bibReferenceNodes1 = (NodeList) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/article/tail/*[name()='ce:bibliography']/*[name()='ce:bibliography-sec']/*[name()='ce:bib-reference']", root, XPathConstants.NODESET);
		NodeList bibReferenceNodes2 = (NodeList) xPath.evaluate("/full-text-retrieval-response/originalText/*[name()='xocs:doc']/*[name()='xocs:serial-item']/converted-article/tail/*[name()='ce:bibliography']/*[name()='ce:bibliography-sec']/*[name()='ce:bib-reference']", root, XPathConstants.NODESET);

		this.parseReferenceNodes(bibReferenceNodes1, references, xPath);
		this.parseReferenceNodes(bibReferenceNodes2, references, xPath);
	}

	private void parseReferenceNodes(NodeList bibReferenceNodes, List<BaseStringEntity> references, XPath xPath) throws XPathExpressionException {
		int numberOfRefs = bibReferenceNodes.getLength();
		for (int ir = 0; ir < numberOfRefs; ir++) {
			Node bibReferenceNode = bibReferenceNodes.item(ir);

			// Check for simple textref
			Node textRefNode = (Node)xPath.evaluate("*[name()='ce:other-ref']/*[name()='ce:textref']", bibReferenceNode, XPathConstants.NODE);
			Node referenceNode = (Node)xPath.evaluate("*[name()='sb:reference']", bibReferenceNode, XPathConstants.NODE);
			if (textRefNode != null) {
				String textRef = textRefNode.getTextContent();
				textRef = this.sanitize(textRef);

				BaseStringEntity reference = new BaseStringEntity();
				reference.setValue(textRef);
				references.add(reference);

			} else if (referenceNode != null) {
				Node contributionNode = (Node)xPath.evaluate("*[name()='sb:contribution']", referenceNode, XPathConstants.NODE);
				Node hostNode = (Node)xPath.evaluate("*[name()='sb:host']", referenceNode, XPathConstants.NODE);

				if (contributionNode != null && hostNode != null) {
					NodeList authorNodes = (NodeList)xPath.evaluate("*[name()='sb:authors']/*[name()='sb:author']", contributionNode, XPathConstants.NODESET);

					String referenceString = "";

					// Authors
					String authorNamesString = "";
					int numberOfAuthors = authorNodes.getLength();
					for (int ia = 0; ia < numberOfAuthors; ia++) {
						Node authorNode = authorNodes.item(ia);

						Node givenNameNode = (Node)xPath.evaluate("*[name()='ce:given-name']", authorNode, XPathConstants.NODE);
						Node surnameNode = (Node)xPath.evaluate("*[name()='ce:surname']", authorNode, XPathConstants.NODE);

						String givenName = givenNameNode == null ? "" : this.sanitize(givenNameNode.getTextContent());
						String surname = surnameNode == null ? "" : this.sanitize(surnameNode.getTextContent());

						authorNamesString += surname + " " + givenName;
						if (ia < numberOfAuthors - 1) {
							authorNamesString += ", ";
						}
					}

					// Document Title
					Node titleNode = (Node)xPath.evaluate("*[name()='sb:title']/*[name()='sb:maintitle']", contributionNode, XPathConstants.NODE);
					String title = titleNode == null ? "" : this.sanitize(titleNode.getTextContent());

					// Journal info
					String journalTitle = "";
					String volume = "";
					String issueNr = "";
					String date = "";
					Node issueNode = (Node)xPath.evaluate("*[name()='sb:issue']", hostNode, XPathConstants.NODE);
					if (issueNode != null) {
						Node journalTitleNode = (Node)xPath.evaluate("*[name()='sb:series']/*[name()='sb:title']/*[name()='sb:maintitle']", issueNode, XPathConstants.NODE);
						journalTitle = journalTitleNode == null ? "" : this.sanitize(journalTitleNode.getTextContent());

						Node volumeNode = (Node)xPath.evaluate("*[name()='sb:series']/*[name()='sb:volume-nr']", issueNode, XPathConstants.NODE);
						volume = volumeNode == null ? "" : this.sanitize(volumeNode.getTextContent());

						Node issueNrNode = (Node)xPath.evaluate("*[name()='sb:issue-nr']", issueNode, XPathConstants.NODE);
						issueNr = issueNrNode == null ? "" : this.sanitize(issueNrNode.getTextContent());

						Node dateNode = (Node)xPath.evaluate("*[name()='sb:date']", issueNode, XPathConstants.NODE);
						date = dateNode == null ? "" : this.sanitize(dateNode.getTextContent());
					}

					String firstPage = "";
					String lastPage = "";
					Node pagesNode = (Node)xPath.evaluate("*[name()='sb:pages']", hostNode, XPathConstants.NODE);
					if (pagesNode != null) {
						Node firstPageNode = (Node)xPath.evaluate("*[name()='sb:first-page']", pagesNode, XPathConstants.NODE);
						firstPage = firstPageNode == null ? "" : this.sanitize(firstPageNode.getTextContent());

						Node lastPageNode = (Node)xPath.evaluate("*[name()='sb:last-page']", pagesNode, XPathConstants.NODE);
						lastPage = lastPageNode == null ? "" : this.sanitize(lastPageNode.getTextContent());
					}

					Node doiNode = (Node)xPath.evaluate("*[name()='ce:doi']", hostNode, XPathConstants.NODE);
					String doi = doiNode == null ? "" : this.sanitize(doiNode.getTextContent());

					// Construct reference string
					if (!authorNamesString.equals("")) {
						referenceString = authorNamesString + " ";
					}

					if (!date.equals("")) {
						referenceString +=  "(" + date + "). ";
					}

					if (!title.equals("")) {
						referenceString += title + ". ";
					}

					if (!journalTitle.equals("")) {
						referenceString += journalTitle;
					}

					if (!volume.equals("")) {
						referenceString += ", " + volume;
					}

					if (!issueNr.equals("")) {
						referenceString += " (" + issueNr + ")";
					}

					if (!firstPage.equals("")) {
						referenceString += ", " + firstPage;
					}

					if (!lastPage.equals("")) {
						referenceString += "-" + lastPage;
					}

					if (!journalTitle.equals("")) {
						referenceString += ". ";
					}

					if (!doi.equals("")) {
						referenceString += "doi:" + doi;
					}

					BaseStringEntity reference = new BaseStringEntity();
					reference.setValue(referenceString);
					references.add(reference);
				}
			}
		}
	}

}
