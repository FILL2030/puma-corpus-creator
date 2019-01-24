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
package eu.ill.puma.webapi.rest.monitoring;

import eu.ill.puma.indexer.manager.IndexationState;

public class MonitoringData {

	private long numberOfPendingTasks;
	private long numberOfActiveThreads;
	private long numberOfThreads;

	private long numberOfImportTasks;
	private long numberOfDownloadTasks;
	private long numberOfResolverTasks;
	private long numberOfAnalysisTasks;
	private long numberOfDeduplicationTasks;
	private long numberOfDocumentsPendingAnalysis;

	private String currentDeduplicationStage;
	private String currentDeduplicationState;

	private long numberOfJobRunner;
	private long numberOfJobs;

	private long numberOfRegisteredAnalyser;
	private long numberOfInstantiatedAnalyser;

	private IndexationState indexationState;
	private Long numberOfPendingIndexation;

	public long getNumberOfPendingTasks() {
		return numberOfPendingTasks;
	}

	public void setNumberOfPendingTasks(long numberOfPendingTasks) {
		this.numberOfPendingTasks = numberOfPendingTasks;
	}

	public long getNumberOfActiveThreads() {
		return numberOfActiveThreads;
	}

	public void setNumberOfActiveThreads(long numberOfActiveThreads) {
		this.numberOfActiveThreads = numberOfActiveThreads;
	}

	public long getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(long numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public long getNumberOfImportTasks() {
		return numberOfImportTasks;
	}

	public void setNumberOfImportTasks(long numberOfImportTasks) {
		this.numberOfImportTasks = numberOfImportTasks;
	}

	public long getNumberOfDownloadTasks() {
		return numberOfDownloadTasks;
	}

	public void setNumberOfDownloadTasks(long numberOfDownloadTasks) {
		this.numberOfDownloadTasks = numberOfDownloadTasks;
	}

	public long getNumberOfResolverTasks() {
		return numberOfResolverTasks;
	}

	public void setNumberOfResolverTasks(long numberOfResolverTasks) {
		this.numberOfResolverTasks = numberOfResolverTasks;
	}

	public long getNumberOfAnalysisTasks() {
		return numberOfAnalysisTasks;
	}

	public void setNumberOfAnalysisTasks(long numberOfAnalysisTasks) {
		this.numberOfAnalysisTasks = numberOfAnalysisTasks;
	}

	public long getNumberOfDeduplicationTasks() {
		return numberOfDeduplicationTasks;
	}

	public void setNumberOfDeduplicationTasks(long numberOfDeduplicationTasks) {
		this.numberOfDeduplicationTasks = numberOfDeduplicationTasks;
	}

	public String getCurrentDeduplicationStage() {
		return currentDeduplicationStage;
	}

	public void setCurrentDeduplicationStage(String currentDeduplicationStage) {
		this.currentDeduplicationStage = currentDeduplicationStage;
	}

	public String getCurrentDeduplicationState() {
		return currentDeduplicationState;
	}

	public void setCurrentDeduplicationState(String currentDeduplicationState) {
		this.currentDeduplicationState = currentDeduplicationState;
	}

	public long getNumberOfDocumentsPendingAnalysis() {
		return numberOfDocumentsPendingAnalysis;
	}

	public void setNumberOfDocumentsPendingAnalysis(long numberOfDocumentsPendingAnalysis) {
		this.numberOfDocumentsPendingAnalysis = numberOfDocumentsPendingAnalysis;
	}

	public long getNumberOfJobRunner() {
		return numberOfJobRunner;
	}

	public void setNumberOfJobRunner(long numberOfJobRunner) {
		this.numberOfJobRunner = numberOfJobRunner;
	}

	public long getNumberOfJobs() {
		return numberOfJobs;
	}

	public void setNumberOfJobs(long numberOfJobs) {
		this.numberOfJobs = numberOfJobs;
	}

	public long getNumberOfRegisteredAnalyser() {
		return numberOfRegisteredAnalyser;
	}

	public void setNumberOfRegisteredAnalyser(long numberOfRegisteredAnalyser) {
		this.numberOfRegisteredAnalyser = numberOfRegisteredAnalyser;
	}

	public long getNumberOfInstantiatedAnalyser() {
		return numberOfInstantiatedAnalyser;
	}

	public void setNumberOfInstantiatedAnalyser(long numberOfInstantiatedAnalyser) {
		this.numberOfInstantiatedAnalyser = numberOfInstantiatedAnalyser;
	}

	public IndexationState getIndexationState() {
		return indexationState;
	}

	public void setIndexationState(IndexationState indexationState) {
		this.indexationState = indexationState;
	}

	public Long getNumberOfPendingIndexation() {
		return numberOfPendingIndexation;
	}

	public void setNumberOfPendingIndexation(Long numberOfPendingIndexation) {
		this.numberOfPendingIndexation = numberOfPendingIndexation;
	}

}
