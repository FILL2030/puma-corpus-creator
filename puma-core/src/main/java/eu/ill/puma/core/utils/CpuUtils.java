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
package eu.ill.puma.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CpuUtils {

	private static OsValidator osValidator = new OsValidator();

	private static final Logger log = LoggerFactory.getLogger(CpuUtils.class);

	private static Integer cpuCoreNumber;

	public static Integer getNumberOfCPUCores() {
		if (cpuCoreNumber == null) {
			String command = "";
			Process process = null;
			int numberOfCores = Runtime.getRuntime().availableProcessors();
			int sockets = 1;

			try {

				if (osValidator.isMac()) {
					command = "sysctl -n machdep.cpu.core_count";
				} else if (osValidator.isUnix()) {
					command = "lscpu";
				} else if (osValidator.isWindows()) {
					command = "cmd /C WMIC CPU Get /Format:List";
				}


				if (osValidator.isMac()) {
					String[] cmd = {"/bin/sh", "-c", command};
					process = Runtime.getRuntime().exec(cmd);
				} else {
					process = Runtime.getRuntime().exec(command);
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;

				while ((line = reader.readLine()) != null) {
					if (osValidator.isMac()) {
						numberOfCores = line.length() > 0 ? Integer.parseInt(line) : 0;
					} else if (osValidator.isUnix()) {
						if (line.contains("Core(s) per socket:") || line.contains("Cœur")) {
							numberOfCores = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
						}
						if (line.contains("Socket(s):")) {
							sockets = Integer.parseInt(line.split("\\s+")[line.split("\\s+").length - 1]);
						}
					} else if (osValidator.isWindows()) {
						if (line.contains("NumberOfCores")) {
							numberOfCores = Integer.parseInt(line.split("=")[1]);
						}
					}
				}

				cpuCoreNumber = numberOfCores * sockets;

			} catch (Exception e) {
				log.error("can not determine the number of cpu core", e);
				cpuCoreNumber = Runtime.getRuntime().availableProcessors();
			}
		}

		return cpuCoreNumber;
	}


	private static class OsValidator {
		private String OS = System.getProperty("os.name").toLowerCase();

		public boolean isWindows() {
			return (OS.indexOf("win") >= 0);
		}

		public boolean isMac() {
			return (OS.indexOf("mac") >= 0);
		}

		public boolean isUnix() {
			return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
		}

		public boolean isSolaris() {
			return (OS.indexOf("sunos") >= 0);
		}

		public String getOS() {
			if (isWindows()) {
				return "win";
			} else if (isMac()) {
				return "osx";
			} else if (isUnix()) {
				return "uni";
			} else if (isSolaris()) {
				return "sol";
			} else {
				return "err";
			}
		}
	}
}



