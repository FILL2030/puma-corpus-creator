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
package eu.ill.puma.importermanager.importer.domain;

import java.util.Date;

public class ResponseMetadata {

	private String nextCursor = null;
	private String previousCursor = null;
	private ImporterStatusEnum status = null;
	private Long first = null;
	private Long count = null;
	private Long totalCount = null;
	private String message = null;
	private Date requestDuration = null;

	public ImporterStatusEnum getStatus() {
		return status;
	}

	public void setStatus(ImporterStatusEnum status) {
		this.status = status;
	}

	public String getNextCursor() {
		return nextCursor;
	}

	public void setNextCursor(String nextCursor) {
		this.nextCursor = nextCursor;
	}

	public String getPreviousCursor() {
		return previousCursor;
	}

	public void setPreviousCursor(String previousCursor) {
		this.previousCursor = previousCursor;
	}

	public Long getFirst() {
		return first;
	}

	public void setFirst(Long first) {
		this.first = first;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Long getTotalCount() {
		return totalCount == null ? -1 : totalCount;
	}

	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getRequestDuration() {
		return requestDuration;
	}

	public void setRequestDuration(Date requestDuration) {
		this.requestDuration = requestDuration;
	}

	public Long getCurrentCount() {
		if (this.count != null && this.first != null) {
			return this.getFirst() + this.getCount() - 1;
		} else {
			return 0L;
		}
	}

}

