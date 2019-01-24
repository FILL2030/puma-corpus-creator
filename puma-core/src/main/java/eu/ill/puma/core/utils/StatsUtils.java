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

import java.util.Collections;
import java.util.List;

/**
 * Created by letreguilly on 25/07/17.
 */
public class StatsUtils {
	public static Double getMean(List<Double> data) {
		Double sum = 0.0;

		for (Double a : data) {
			sum += a;
		}

		if (data.size() > 0) {
			return sum / data.size();
		} else {
			return null;
		}
	}

	public static Double getVariance(List<Double> data) {
		Double mean = getMean(data);
		Double temp = 0.0;

		for (Double a : data) {
			temp += (a - mean) * (a - mean);
		}

		if (data.size() > 1) {
			return temp / (data.size() - 1);
		} else {
			return null;
		}
	}

	public static Double getStandardDeviation(List<Double> data) {
		return Math.sqrt(getVariance(data));
	}

	public static Double getMedian(List<Double> data) {
		Collections.sort(data);

		if (data.size() % 2 == 0) {
			return (data.get((data.size() / 2) - 1) + data.get(data.size() / 2)) / 2.0;
		}

		return data.get(data.size() / 2);
	}
}
