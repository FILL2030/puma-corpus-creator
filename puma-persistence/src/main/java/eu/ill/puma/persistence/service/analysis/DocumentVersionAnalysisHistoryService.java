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
package eu.ill.puma.persistence.service.analysis;

import eu.ill.puma.core.utils.StatsUtils;
import eu.ill.puma.persistence.domain.analysis.DocumentVersionAnalysisHistory;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import eu.ill.puma.persistence.repository.analyser.DocumentVersionAnalysisHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentVersionAnalysisHistoryService {

	@Autowired
	private DocumentVersionAnalysisHistoryRepository repository;


	public DocumentVersionAnalysisHistory getForDocumentVersionAndSuccessfulAnalyser(DocumentVersion documentVersion, String analyserName) {
		return this.repository.getForDocumentVersionAndSuccessfulAnalyser(documentVersion, analyserName);
	}

	public DocumentVersionAnalysisHistory getForDocumentVersion(DocumentVersion documentVersion, String analyserName) {
		return this.repository.getForDocumentVersion(documentVersion, analyserName);
	}

	public List<DocumentVersionAnalysisHistory> getAllForDocumentVersion(DocumentVersion documentVersion) {
		return this.repository.getAllForDocumentVersion(documentVersion);
	}

	public List<DocumentVersionAnalysisHistory> getAllSuccessfulForDocumentVersion(DocumentVersion documentVersion) {
		return this.repository.getAllSuccessfulForDocumentVersion(documentVersion);
	}

	public List<String> getAllSuccessfulAnalysersForDocumentVersion(DocumentVersion documentVersion) {
		List<DocumentVersionAnalysisHistory>  allAnalysisHistory = this.repository.getAllSuccessfulForDocumentVersion(documentVersion);
		List<String> successfulAnalyserNames = new ArrayList<>();
		for (DocumentVersionAnalysisHistory analysisHistory : allAnalysisHistory) {
			if (!successfulAnalyserNames.contains(analysisHistory.getAnalyserName())) {
				successfulAnalyserNames.add(analysisHistory.getAnalyserName());
			}
		}

		return successfulAnalyserNames;
	}

	public List<Long> getAllDocumentVersionIdForAnalyserNameContains(String analyserName){
		return this.repository.getAllDocumentVersionIdForAnalyserNameContains(analyserName);
	}

	public DocumentVersionAnalysisHistory save(DocumentVersionAnalysisHistory history) {
		if (history.getId() == null) {
			return this.repository.persist(history);

		} else {
			return this.repository.merge(history);
		}
	}

	public Double getMeanTimeForAnalyser(String analyserName) {
		//get data
		List<Double> doubleList = this.collectDurationDataForAnalyser(analyserName);

		//get value
		return StatsUtils.getMean(doubleList);
	}

	public Double getMedianTimeForAnalyser(String analyserName) {
		//get data
		List<Double> doubleList = this.collectDurationDataForAnalyser(analyserName);

		//get value
		return StatsUtils.getMedian(doubleList);
	}

	public Double getStrandardDeviationTimeForAnalyser(String analyserName) {
		//get data
		List<Double> doubleList = this.collectDurationDataForAnalyser(analyserName);

		//get value
		return StatsUtils.getStandardDeviation(doubleList);
	}

	public Double getVarianceTimeForAnalyser(String analyserName) {
		//get data
		List<Double> doubleList = this.collectDurationDataForAnalyser(analyserName);

		//get value
		return StatsUtils.getVariance(doubleList);
	}

	public void delete(DocumentVersionAnalysisHistory history){
		this.repository.delete(history);
	}

	public List<DocumentVersionAnalysisHistory> getAll(){
		return this.repository.getAll();
	}

	private List<Double> collectDurationDataForAnalyser(String analyserName) {
		//get data
		List<DocumentVersionAnalysisHistory> historyList = this.repository.getAllForAnalyser(analyserName);

		//get durations
		return historyList.stream()
				.map(history -> history.getDuration().doubleValue())
				.collect(Collectors.toList());
	}

}
