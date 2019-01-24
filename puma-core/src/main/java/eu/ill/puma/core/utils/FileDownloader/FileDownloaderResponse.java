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
package eu.ill.puma.core.utils.FileDownloader;

/**
 * Created by letreguilly on 06/07/17.
 */
public class FileDownloaderResponse {

	private byte[] fileData;
	private String mimeType;
	private FileDownloaderResponseStatus status = FileDownloaderResponseStatus.OK;
	private String message;

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public FileDownloaderResponseStatus getStatus() {
		return status;
	}

	public void setStatus(FileDownloaderResponseStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isPdf() {
		if (this.fileData != null && this.fileData.length > 4) {
			return this.fileData[0] == 0x25 &&
				this.fileData[1] == 0x50 &&
				this.fileData[2] == 0x44 &&
				this.fileData[3] == 0x46;
		}
		return false;
	}
}
