package net.sf.sessionAnalysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.tree.TreeNode;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import kieker.analysis.AnalysisController;
import kieker.common.configuration.Configuration;
import kieker.common.util.signature.Signature;
import kieker.tools.traceAnalysis.filter.traceReconstruction.InvalidTraceException;
import kieker.tools.traceAnalysis.systemModel.AllocationComponent;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.ComponentType;
import kieker.tools.traceAnalysis.systemModel.Execution;
import kieker.tools.traceAnalysis.systemModel.ExecutionContainer;
import kieker.tools.traceAnalysis.systemModel.ExecutionTrace;
import kieker.tools.traceAnalysis.systemModel.ExecutionTraceBasedSession;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;

/**
 * 
 * @author Andre van Hoorn
 * 
 */
public class SessionAnalyzer {
	private static final String INPUT_SESSIONS_DAT_FN = "../../evaluation/SPECjEnterprise-data/kieker-20110929-14382537-UTC-blade3-KIEKER-SPECjEnterprise2010-20-min-excerpt-sessions.dat";

	private static AllocationComponent allocationComponent;

	private static final ConcurrentHashMap<String, Operation> actionsToOperationsTable = new ConcurrentHashMap<String, Operation>();

	private static int curTraceId = 0;

	public static void main(String[] args) {

		Collection<ExecutionTraceBasedSession> sessions = extractSessions();

		/*
		 * Session length histogram. Results can be compared analysis on raw
		 * data: cat
		 * ../evaluation/SPECjEnterprise-data/kieker-20110929-14382537-
		 * UTC-blade3-KIEKER-SPECjEnterprise2010-20-min-excerpt-sessions.dat |
		 * awk -F ";" '{print NF-1}' | sort -n | uniq -c | wc -l
		 */
		TreeMap<Integer, AtomicInteger> sessionLengths = computeSessionLengthStatistics(sessions);
		System.out.println(sessionLengths);
		System.out.println(sessionLengths.size());

		{
			List<Double> lengths = new LinkedList<Double>();
			for (Entry<Integer, AtomicInteger> entry : sessionLengths.entrySet()) {
				for (int i=0; i<entry.getValue().get(); i++) {
					lengths.add((double)entry.getKey());
				}
			}
			Mean meanObj = new Mean();
			double[] lengthsPrim = ArrayUtils.toPrimitive(lengths.toArray(new Double[]{}));
			double meanSessionLength = meanObj.evaluate(lengthsPrim);
			System.out.println("Mean length: " + meanSessionLength);
			StandardDeviation stdDevObj = new StandardDeviation();
			double stdDev = stdDevObj.evaluate(lengthsPrim);
			System.out.println("Standard dev: " + stdDev);
		}

	}

	private static Collection<ExecutionTraceBasedSession> extractSessions() {
		Collection<ExecutionTraceBasedSession> sessions = new ArrayList<ExecutionTraceBasedSession>();
		FileInputStream fis;
		BufferedReader reader = null;

		SystemModelRepository systemModelRepository = initSystemModelRepository();

		try {
			fis = new FileInputStream(INPUT_SESSIONS_DAT_FN);
			reader = new BufferedReader(new InputStreamReader(fis));

			String line = reader.readLine();
			while (line != null) {
				line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] sessonInfoSplit = line.split(";");
				String sessionId = sessonInfoSplit[0];

				// Now extract information about the actions
				ExecutionTraceBasedSession session = new ExecutionTraceBasedSession(
						sessionId);
				sessions.add(session);
				/* skip first field, which contains the session id */
				for (int i = 1; i < sessonInfoSplit.length; i++) {
					String actionInfo = sessonInfoSplit[i];
					String[] actionInfoSplit = actionInfo.split(":");
					final String invokedAction = actionInfoSplit[0].replaceAll(
							"\"", "");
					final long actionStartTime = Long
							.parseLong(actionInfoSplit[1]);
					final long actionEndTime = Long
							.parseLong(actionInfoSplit[2]);
					ExecutionTrace trace = createExecutionTraceForActionInformation(
							systemModelRepository, sessionId, invokedAction,
							actionStartTime, actionEndTime);
					session.addTrace(trace);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidTraceException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sessions;
	}

	/**
	 * Returns a histogram of the lengths of the given sessions. The returned
	 * TreeMap contains the observed session lengths as keys and their absolute
	 * frequencies values.
	 * 
	 * @param sessions
	 * @return
	 */
	private static TreeMap<Integer, AtomicInteger> computeSessionLengthStatistics(
			Collection<ExecutionTraceBasedSession> sessions) {
		TreeMap<Integer, AtomicInteger> sessionLengthHistogram = new TreeMap<Integer, AtomicInteger>();
		for (ExecutionTraceBasedSession session : sessions) {
			final int curLength = session.getContainedTraces().size();
			AtomicInteger curFrequency = sessionLengthHistogram.get(curLength);
			if (curFrequency == null) { // session length not observed before
				curFrequency = new AtomicInteger(0);
				sessionLengthHistogram.put(curLength, curFrequency);
			}
			curFrequency.incrementAndGet();
		}
		return sessionLengthHistogram;
	}

	private static SystemModelRepository initSystemModelRepository() {
		AnalysisController analysisController = new AnalysisController();
		SystemModelRepository systemModelRepository = new SystemModelRepository(
				new Configuration(), analysisController);
		ComponentType componentType = new ComponentType(55, "System");
		ExecutionContainer executionContainer = new ExecutionContainer(66,
				null, "Srv");
		AssemblyComponent assemblyComponent = new AssemblyComponent(44,
				"system", componentType);
		allocationComponent = new AllocationComponent(33, assemblyComponent,
				executionContainer);
		return systemModelRepository;
	}

	private static int nextOperationId = 0;

	private static Operation lookupOrCreateOperationForActionName(
			String actionName) {
		Operation op = actionsToOperationsTable.get(actionName);
		if (op != null) {
			return op;
		}

		// Operation for action does not exist, create:
		AssemblyComponent assemblyComponent = allocationComponent
				.getAssemblyComponent();
		ComponentType componentType = assemblyComponent.getType();
		op = new Operation(nextOperationId++, componentType, new Signature(
				actionName, new String[] {}, "void", new String[] {}));
		actionsToOperationsTable.put(actionName, op);
		componentType.addOperation(op);

		return op;
	}

	private static ExecutionTrace createExecutionTraceForActionInformation(
			SystemModelRepository systemModelRepository, String sessionId,
			String invokedAction, long actionStartTime, long actionEndTime)
			throws InvalidTraceException {
		ExecutionTrace executionTrace = new ExecutionTrace(curTraceId++);

		Execution execution = new Execution(
				lookupOrCreateOperationForActionName(invokedAction),
				allocationComponent, executionTrace.getTraceId(), sessionId, 0,
				0, actionStartTime, actionEndTime, false);
		executionTrace.add(execution);

		return executionTrace;
	}
}
