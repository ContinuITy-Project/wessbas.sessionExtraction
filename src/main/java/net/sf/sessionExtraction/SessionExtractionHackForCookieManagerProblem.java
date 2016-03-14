/***************************************************************************
 * Copyright (c) 2016 the WESSBAS project
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
 ***************************************************************************/


package net.sf.sessionExtraction;

import kieker.analysis.AnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.forward.ListCollectionFilter;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.executionRecordTransformation.ExecutionRecordTransformationFilter;
import kieker.tools.traceAnalysis.filter.sessionReconstruction.SessionReconstructionFilter;
import kieker.tools.traceAnalysis.filter.traceReconstruction.TraceReconstructionFilter;
import kieker.tools.traceAnalysis.systemModel.ExecutionTraceBasedSession;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;

/**
 * 
 * @author Andre van Hoorn
 *
 */
public class SessionExtractionHackForCookieManagerProblem {
//	private static final String INPUT_MONITORING_LOG_FN = "../../evaluation/SPECjEnterprise-data/kieker-20110929-14382537-UTC-blade3-KIEKER-SPECjEnterprise2010-20-min-excerpt/";
//	private static final String OUTPUT_SESSIONS_DAT_FN = "../../evaluation/SPECjEnterprise-data/kieker-20110929-14382537-UTC-blade3-KIEKER-SPECjEnterprise2010-20-min-excerpt-sessions.dat";

	private static final String INPUT_MONITORING_LOG_FN = "../../evaluation/20140826-SPECjEnterprise/4-loadTestsResults/specj_25p_50b_25m_euclidean_4_Cluster/kieker-20140907-164744465-UTC-VAIO-KIEKER-SINGLETON/";
	private static final String OUTPUT_SESSIONS_DAT_FN = "../../evaluation/20140826-SPECjEnterprise/4-loadTestsResults/specj_25p_50b_25m_euclidean_4_Cluster/sessions-correctedWithHack.dat";	
	
	public static void main(final String[] args) throws IllegalStateException, AnalysisConfigurationException {
		
		final AnalysisController analysisController = new AnalysisController();

		// Initialize and register the list reader
		Configuration fsReaderConfig = new Configuration();
		fsReaderConfig.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, INPUT_MONITORING_LOG_FN);
		final FSReader reader = new FSReader(fsReaderConfig, analysisController);

		// Initialize and register the system model repository
		final SystemModelRepository systemModelRepository = new SystemModelRepository(new Configuration(), analysisController);

//		final TeeFilter teeFilter1 = new TeeFilter(new Configuration(), analysisController);
//		analysisController.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, 
//				teeFilter1, TeeFilter.INPUT_PORT_NAME_EVENTS);

		
		// Initialize, register and connect the execution record transformation filter
		final ExecutionRecordTransformationFilter executionRecordTransformationFilter = new ExecutionRecordTransformationFilter(new Configuration(),
				analysisController);
		analysisController.connect(executionRecordTransformationFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
		analysisController.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS,
				executionRecordTransformationFilter, ExecutionRecordTransformationFilter.INPUT_PORT_NAME_RECORDS);

		// Initialize, register and connect the trace reconstruction filter
		final TraceReconstructionFilter traceReconstructionFilter = new TraceReconstructionFilter(new Configuration(), analysisController);
		analysisController.connect(traceReconstructionFilter,
				AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, systemModelRepository);
		analysisController.connect(executionRecordTransformationFilter, ExecutionRecordTransformationFilter.OUTPUT_PORT_NAME_EXECUTIONS,
				traceReconstructionFilter, TraceReconstructionFilter.INPUT_PORT_NAME_EXECUTIONS);

		// Initialize, register and connect the session reconstruction filter
		final Configuration bareSessionReconstructionFilterConfiguration = new Configuration();
		bareSessionReconstructionFilterConfiguration.setProperty(SessionReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_THINK_TIME, 
				SessionReconstructionFilter.CONFIG_PROPERTY_VALUE_MAX_THINK_TIME);


		final SessionReconstructionFilter sessionReconstructionFilter = new SessionReconstructionFilter(bareSessionReconstructionFilterConfiguration,
				analysisController);
		analysisController.connect(traceReconstructionFilter, TraceReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE,
				sessionReconstructionFilter, SessionReconstructionFilter.INPUT_PORT_NAME_EXECUTION_TRACES);

		// Initialize, register and connect the list collection filter
		final ListCollectionFilter<ExecutionTraceBasedSession> listCollectionFilter = new ListCollectionFilter<ExecutionTraceBasedSession>(new Configuration(),
				analysisController);
		analysisController.connect(sessionReconstructionFilter, SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS,
				listCollectionFilter, ListCollectionFilter.INPUT_PORT_NAME);
		
//		final TeeFilter teeFilter = new TeeFilter(new Configuration(), analysisController);
//		analysisController.connect(sessionReconstructionFilter, SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS, 
//				teeFilter, TeeFilter.INPUT_PORT_NAME_EVENTS);

		final Configuration sessionDatWriterConfiguration = new Configuration();
		sessionDatWriterConfiguration.setProperty(SessionDatWriterPlugin.CONFIG_PROPERTY_NAME_STREAM, OUTPUT_SESSIONS_DAT_FN);
		sessionDatWriterConfiguration.setProperty(SessionDatWriterPlugin.CONFIG_PROPERTY_NAME_APPEND, "false");
		final SessionDatWriterPluginHackForCookieManagerProblem sessionsDatWriter = new SessionDatWriterPluginHackForCookieManagerProblem(sessionDatWriterConfiguration, analysisController);
		analysisController.connect(sessionReconstructionFilter, SessionReconstructionFilter.OUTPUT_PORT_NAME_EXECUTION_TRACE_SESSIONS, 
				sessionsDatWriter, SessionDatWriterPlugin.INPUT_PORT_NAME_SESSIONS);
		
		analysisController.run();
	}
	
}
