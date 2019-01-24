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
package eu.ill.puma.analysis.factory;

import eu.ill.puma.analysis.analyser.DocumentAnalyser;
import eu.ill.puma.analysis.exception.AnalyserNotFoundException;
import eu.ill.puma.analysis.exception.AnalysisException;
import eu.ill.puma.analysis.exception.UnPreparedAnalyserException;
import eu.ill.puma.persistence.domain.document.DocumentVersion;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by letreguilly on 21/07/17.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners({
		DependencyInjectionTestExecutionListener.class,
		TransactionalTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AnalyserFactoryTest {

	@Autowired
	private AnalyserFactory analyserFactory;

	@Test
	public void testFactory() throws Exception {
		Assert.assertNotNull(analyserFactory);
		Assert.assertTrue(analyserFactory.getNumberOfRegisteredAnalysers() > 0);

		DocumentAnalyser analyser = analyserFactory.getAnalyserForName("elsevierxml");
		Assert.assertNotNull(analyser);
		Assert.assertEquals(0, analyserFactory.getNumberOfInstantiatedAnalyser());

		DocumentVersion document = new DocumentVersion();
		document.setId(1l);
		analyser.analyse(document);
		Assert.assertEquals(1, analyserFactory.getNumberOfInstantiatedAnalyser());
	}

	@Test(expected = AnalyserNotFoundException.class)
	public void testException() throws Exception {
		analyserFactory.getAnalyserForName("gffdsgsfd");
	}

	@Test
	public void createMaxInstance() throws Exception {
		List<AnalysisThread> threads = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			AnalysisThread analysisThread = new AnalysisThread(analyserFactory.getAnalyserForName("test"));
			analysisThread.start();
			threads.add(analysisThread);
		}

		for (AnalysisThread thread : threads) {
			thread.join();
		}

		Assert.assertEquals(analyserFactory.getMaxInstances("test"), analyserFactory.getNumberOfInstantiatedAnalyser("test"));
	}

	private static class AnalysisThread extends Thread {
		private DocumentAnalyser analyser;

		public AnalysisThread(DocumentAnalyser analyser) {
			this.analyser = analyser;
		}

		public void run() {
			try {
				System.out.println("Run analyser");
				this.analyser.analyse(new DocumentVersion());
			} catch (UnPreparedAnalyserException e) {
				e.printStackTrace();
			} catch (AnalysisException e) {
				e.printStackTrace();
			}
		}
	}

}
