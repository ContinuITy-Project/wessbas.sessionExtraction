/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

import io.github.wessbas.kiekerExtensions.record.ServletEntryRecord;

import java.util.concurrent.ConcurrentHashMap;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;

/**
 * Reads {@link ServletEntryRecord}s and stores them in order to make the information 
 * available to further filters. Note that the way we make the map available to 
 * further filters is kind of a hack. 
 * 
 * @author Andre van Hoorn
 * 
 */
@Plugin(
		description = "A filter extracting session information")
public final class ServletInfoExtractionFilter extends AbstractFilterPlugin {

	/**
	 * The name of the input port receiving the incoming events.
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvents";

	private final ConcurrentHashMap<Long, ServletEntryRecord> servletInformation = new ConcurrentHashMap<Long, ServletEntryRecord>();
	
	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param configuration
	 *            The configuration for this component.
	 * @param projectContext
	 *            The project context for this component.
	 */
	public ServletInfoExtractionFilter(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Configuration getCurrentConfiguration() {
		return new Configuration();
	}

	/**
	 * This method represents the input port of this filter.
	 * 
	 * @param event
	 *            The next event.
	 */
	@InputPort(name = INPUT_PORT_NAME_EVENTS, eventTypes = { ServletEntryRecord.class }, description = "Receives incoming servlet data")
	public final void inputEvent(final ServletEntryRecord event) {
		this.servletInformation.put(event.getTraceId(), event);
	}

	public ConcurrentHashMap<Long, ServletEntryRecord> getServletInformation() {
		return servletInformation;
	}
}
