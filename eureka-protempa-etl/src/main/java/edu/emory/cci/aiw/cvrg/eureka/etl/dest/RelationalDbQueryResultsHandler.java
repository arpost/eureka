package edu.emory.cci.aiw.cvrg.eureka.etl.dest;

/*
 * #%L
 * Eureka Protempa ETL
 * %%
 * Copyright (C) 2012 - 2015 Emory University
 * %%
 * This program is dual licensed under the Apache 2 and GPLv3 licenses.
 * 
 * Apache License, Version 2.0:
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * GNU General Public License version 3:
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import edu.emory.cci.aiw.cvrg.eureka.common.entity.RelDbDestinationEntity;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.RelDbDestinationTableColumnEntity;
import edu.emory.cci.aiw.i2b2etl.dest.config.DatabaseSpec;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.arp.javautil.sql.ConnectionSpec;
import org.neo4j.helpers.ArrayUtil;
import org.protempa.KnowledgeSource;
import org.protempa.KnowledgeSourceCache;
import org.protempa.KnowledgeSourceCacheFactory;
import org.protempa.KnowledgeSourceReadException;
import org.protempa.PropositionDefinition;
import org.protempa.dest.AbstractQueryResultsHandler;
import org.protempa.dest.QueryResultsHandlerCloseException;
import org.protempa.dest.QueryResultsHandlerProcessingException;
import org.protempa.dest.QueryResultsHandlerValidationFailedException;
import org.protempa.dest.table.RelDbTabularWriter;
import org.protempa.dest.table.TableColumnSpec;
import org.protempa.dest.table.TabularWriterException;
import org.protempa.proposition.Proposition;
import org.protempa.proposition.UniqueId;
import org.protempa.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Andrew Post
 */
public class RelationalDbQueryResultsHandler extends AbstractQueryResultsHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RelationalDbQueryResultsHandler.class);

	private final RelDbDestinationEntity config;
	private Map<String, RelDbTabularWriter> writers;
	private Map<String, List<TableColumnSpec>> tableColumnSpecs;
	private final Map<String, Set<String>> rowPropositionIdMap;
	private final KnowledgeSource knowledgeSource;
	private KnowledgeSourceCache ksCache;
	private final RelDbDestinationEntity relDbDestinationEntity;

	RelationalDbQueryResultsHandler(Query query, RelDbDestinationEntity inRelDbDestinationEntity, KnowledgeSource inKnowledgeSource) {
		assert inRelDbDestinationEntity != null : "inRelDbDestinationEntity cannot be null";
		this.config = inRelDbDestinationEntity;
		this.knowledgeSource = inKnowledgeSource;
		this.rowPropositionIdMap = new HashMap<>();
		this.relDbDestinationEntity = inRelDbDestinationEntity;
	}

	@Override
	public void validate() throws QueryResultsHandlerValidationFailedException {
	}

	@Override
	public void start(Collection<PropositionDefinition> cache) throws QueryResultsHandlerProcessingException {
		try {
			List<String> tableNames = this.config.getTableColumns()
					.stream()
					.map(RelDbDestinationTableColumnEntity::getTableName)
					.distinct()
					.collect(Collectors.toCollection(ArrayList::new));
			this.writers = new HashMap<>();
			DatabaseSpecFactory databaseSpecFactory = new DatabaseSpecFactory();
			DatabaseSpec dbSpec = databaseSpecFactory.getInstance(this.relDbDestinationEntity.getConnect(), this.relDbDestinationEntity.getUser(), this.relDbDestinationEntity.getPassword());
			ConnectionSpec connectionSpec = dbSpec.toConnectionSpec();
			for (int i = 0, n = tableNames.size(); i < n; i++) {
				String tableName = tableNames.get(i);
				String[] questionMarkArr = new String[this.relDbDestinationEntity.getTableColumns().size()];
				Arrays.fill(questionMarkArr, "?");
				String q = "INSERT INTO " + tableName + " VALUES (" + ArrayUtil.join(questionMarkArr, ",") + ")";
				this.writers.put(tableName, new RelDbTabularWriter(connectionSpec, q));
			}
		} catch (SQLException ex) {
			throw new QueryResultsHandlerProcessingException(ex);
		}

		List<RelDbDestinationTableColumnEntity> tableColumns = this.config.getTableColumns();
		Collections.sort(tableColumns, new Comparator<RelDbDestinationTableColumnEntity>() {
			@Override
			public int compare(RelDbDestinationTableColumnEntity o1, RelDbDestinationTableColumnEntity o2) {
				return o1.getRank().compareTo(o2.getRank());
			}

		});
		this.tableColumnSpecs = new HashMap<>();
		for (RelDbDestinationTableColumnEntity tableColumn : tableColumns) {
			String format = tableColumn.getFormat();
			TableColumnSpecFormat linksFormat = new TableColumnSpecFormat(tableColumn.getColumnName(), format != null ? new SimpleDateFormat(format) : null);
			TableColumnSpecWrapper tableColumnSpecWrapper = toTableColumnSpec(tableColumn, linksFormat);
			String pid = tableColumnSpecWrapper.getPropId();
			if (pid != null) {
				Set<String> propIds;
				try {
					propIds = this.knowledgeSource.collectPropIdDescendantsUsingInverseIsA(pid);
				} catch (KnowledgeSourceReadException ex) {
					throw new QueryResultsHandlerProcessingException(ex);
				}
				for (String propId : propIds) {
					org.arp.javautil.collections.Collections.putSet(this.rowPropositionIdMap, tableColumn.getTableName(), propId);
				}
			}
			org.arp.javautil.collections.Collections.putList(this.tableColumnSpecs, tableColumn.getTableName(), tableColumnSpecWrapper.getTableColumnSpec());
		}

		LOGGER.debug("Row concepts: {}", this.rowPropositionIdMap);

		for (Map.Entry<String, List<TableColumnSpec>> me : this.tableColumnSpecs.entrySet()) {
			List<String> columnNames = new ArrayList<>();
			for (TableColumnSpec columnSpec : me.getValue()) {
				String[] colNames;
				try {
					colNames = columnSpec.columnNames(this.knowledgeSource);
				} catch (KnowledgeSourceReadException ex) {
					throw new AssertionError("Should never happen");
				}
				for (String colName : colNames) {
					columnNames.add(colName);
				}
			}
			RelDbTabularWriter writer = this.writers.get(me.getKey());
			try {
				for (String columnName : columnNames) {
					writer.writeString(columnName);
				}
				writer.newRow();
			} catch (TabularWriterException ex) {
				throw new QueryResultsHandlerProcessingException(ex);
			}
		}

		try {
			this.ksCache = new KnowledgeSourceCacheFactory().getInstance(this.knowledgeSource, cache, true);
		} catch (KnowledgeSourceReadException ex) {
			throw new QueryResultsHandlerProcessingException(ex);
		}

	}

	@Override
	public void handleQueryResult(String keyId, List<Proposition> propositions, Map<Proposition, List<Proposition>> forwardDerivations, Map<Proposition, List<Proposition>> backwardDerivations, Map<UniqueId, Proposition> references) throws QueryResultsHandlerProcessingException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Data for keyId {}: {}", new Object[]{keyId, propositions});
		}

		for (Map.Entry<String, List<TableColumnSpec>> me : this.tableColumnSpecs.entrySet()) {
			String tableName = me.getKey();
			List<TableColumnSpec> columnSpecs = me.getValue();
			int n = columnSpecs.size();
			RelDbTabularWriter writer = this.writers.get(tableName);
			Set<String> rowPropIds = this.rowPropositionIdMap.get(tableName);
			if (rowPropIds != null) {
				for (Proposition prop : propositions) {
					if (rowPropIds.contains(prop.getId())) {
						try {
							for (int i = 0; i < n; i++) {
								TableColumnSpec columnSpec = columnSpecs.get(i);
								columnSpec.columnValues(keyId, prop, forwardDerivations, backwardDerivations, references, this.ksCache, writer);
							}
							writer.newRow();
						} catch (TabularWriterException ex) {
							throw new QueryResultsHandlerProcessingException("Could not write row" + ex);
						}
					}
				}
			}
		}
	}

	@Override
	public void finish() throws QueryResultsHandlerProcessingException {
	}

	@Override
	public void close() throws QueryResultsHandlerCloseException {
		QueryResultsHandlerCloseException exception = null;
		if (this.writers != null) {
			for (RelDbTabularWriter writer : this.writers.values()) {
				try {
					writer.close();
				} catch (TabularWriterException ex) {
					if (exception != null) {
						exception.addSuppressed(ex);
					} else {
						exception = new QueryResultsHandlerCloseException(ex);
					}
				}
				this.writers = null;
			}
		}
		if (exception != null) {
			throw exception;
		}
	}

	private static TableColumnSpecWrapper toTableColumnSpec(RelDbDestinationTableColumnEntity tableColumn, TableColumnSpecFormat linksFormat) throws QueryResultsHandlerProcessingException {
		try {
			return (TableColumnSpecWrapper) linksFormat.parseObject(tableColumn.getPath());
		} catch (ParseException ex) {
			throw new QueryResultsHandlerProcessingException(ex);
		}
	}

}
