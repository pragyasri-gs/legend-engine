// Copyright 2022 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.persistence.components;

import org.finos.legend.engine.persistence.components.common.DatasetFilter;
import org.finos.legend.engine.persistence.components.common.FilterType;
import org.finos.legend.engine.persistence.components.common.StatisticName;
import org.finos.legend.engine.persistence.components.ingestmode.DeriveMainDatasetSchemaFromStaging;
import org.finos.legend.engine.persistence.components.logicalplan.conditions.And;
import org.finos.legend.engine.persistence.components.logicalplan.conditions.GreaterThan;
import org.finos.legend.engine.persistence.components.logicalplan.conditions.LessThan;
import org.finos.legend.engine.persistence.components.logicalplan.conditions.Or;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.DataType;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.Dataset;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.DatasetDefinition;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.DerivedDataset;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.Field;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.FieldType;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.FilteredDataset;
import org.finos.legend.engine.persistence.components.logicalplan.datasets.SchemaDefinition;
import org.finos.legend.engine.persistence.components.logicalplan.values.FieldValue;
import org.finos.legend.engine.persistence.components.logicalplan.values.NumericalValue;
import org.finos.legend.engine.persistence.components.logicalplan.values.StringValue;
import org.finos.legend.engine.persistence.components.relational.api.DataSplitRange;
import org.finos.legend.engine.persistence.components.relational.api.GeneratorResult;
import org.finos.legend.engine.persistence.components.scenarios.TestScenario;
import org.finos.legend.engine.persistence.components.util.LogicalPlanUtils;
import org.junit.jupiter.api.Assertions;

import java.time.Clock;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BaseTest
{
    protected final ZonedDateTime fixedZonedDateTime_2000_01_01 = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
    protected final Clock fixedClock_2000_01_01 = Clock.fixed(fixedZonedDateTime_2000_01_01.toInstant(), ZoneOffset.UTC);

    protected String schema = "my_schema";

    protected String mainDbName = "mydb";
    protected String mainTableName = "main";
    protected String mainTableAlias = "sink";

    protected String stagingDbName = "mydb";
    protected String stagingTableName = "staging";
    protected String stagingTableAlias = "stage";

    protected String tempDbName = "mydb";
    protected String tempTableName = "temp";
    protected String tempTableAlias = "temp";

    protected String tempWithDeleteIndicatorDbName = "mydb";
    protected String tempWithDeleteIndicatorTableName = "tempWithDeleteIndicator";
    protected String tempWithDeleteIndicatorTableAlias = "tempWithDeleteIndicator";

    protected String stagingWithoutDuplicatesDbName = "mydb";
    protected String stagingTableWithoutDuplicatesName = "stagingWithoutDuplicates";
    protected String stagingTableWithoutDuplicatesAlias = "stage";

    protected String digestField = "digest";
    protected String digestUdf = "LAKEHOUSE_MD5";
    protected String versionField = "version";
    protected String bizDateField = "biz_date";
    protected String snapshotIdField = "snapshot_id";
    protected String dataSplitField = "data_split";
    protected String batchUpdateTimeField = "batch_update_time";
    protected String batchIdInField = "batch_id_in";
    protected String batchIdOutField = "batch_id_out";
    protected String batchTimeInField = "batch_time_in";
    protected String batchTimeOutField = "batch_time_out";
    protected String deleteIndicatorField = "delete_indicator";
    protected String[] deleteIndicatorValues = new String[]{"yes", "1", "true"};
    protected Boolean[] deleteIndicatorBooleanValues = new Boolean[]{true};
    protected String validityFromReferenceField = "validity_from_reference";
    protected String validityThroughReferenceField = "validity_through_reference";
    protected String validityFromTargetField = "validity_from_target";
    protected String validityThroughTargetField = "validity_through_target";

    protected String[] partitionKeys = new String[]{"biz_date"};
    protected Map<String, Set<String>> partitionFilter = new HashMap<String, Set<String>>()
    {{
        put("biz_date", new HashSet<>(Arrays.asList("2000-01-01 00:00:00", "2000-01-02 00:00:00")));
    }};

    // Base Columns: Primary keys : id, name
    protected Field id = Field.builder().name("id").type(FieldType.of(DataType.INT, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field tinyIntId = Field.builder().name("id").type(FieldType.of(DataType.TINYINT, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field tinyIntString = Field.builder().name("id").type(FieldType.of(DataType.VARCHAR, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field name = Field.builder().name("name").type(FieldType.of(DataType.VARCHAR, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field nameModified = Field.builder().name("name").type(FieldType.of(DataType.VARCHAR, 64, null)).primaryKey(true).build();
    protected Field amount = Field.builder().name("amount").type(FieldType.of(DataType.DOUBLE, Optional.empty(), Optional.empty())).build();
    protected Field floatAmount = Field.builder().name("amount").type(FieldType.of(DataType.FLOAT, Optional.empty(), Optional.empty())).build();
    protected Field bizDate = Field.builder().name("biz_date").type(FieldType.of(DataType.DATE, Optional.empty(), Optional.empty())).build();

    // Bitemporal Columns:
    protected Field validityFromReference = Field.builder().name(validityFromReferenceField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field validityThroughReference = Field.builder().name(validityThroughReferenceField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).build();
    protected Field validityFromTarget = Field.builder().name(validityFromTargetField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field validityThroughTarget = Field.builder().name(validityThroughTargetField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).build();

    // Problematic Columns
    protected Field idNonPrimary = Field.builder().name("id").type(FieldType.of(DataType.INT, Optional.empty(), Optional.empty())).build();
    protected Field nameNonPrimary = Field.builder().name("name").type(FieldType.of(DataType.VARCHAR, Optional.empty(), Optional.empty())).build();
    protected Field batchIdInNonPrimary = Field.builder().name(batchIdInField).type(FieldType.of(DataType.INT, Optional.empty(), Optional.empty())).build();
    protected Field batchTimeInNonPrimary = Field.builder().name(batchTimeInField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).build();

    // Milestoning Columns
    protected Field digest = Field.builder().name(digestField).type(FieldType.of(DataType.STRING, Optional.empty(), Optional.empty())).build();
    protected Field version = Field.builder().name(versionField).type(FieldType.of(DataType.INT, Optional.empty(), Optional.empty())).build();
    protected Field dataSplit = Field.builder().name(dataSplitField).type(FieldType.of(DataType.BIGINT, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field batchUpdateTime = Field.builder().name(batchUpdateTimeField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).primaryKey(true).build();

    protected Field batchUpdateTimeNonPK = Field.builder().name(batchUpdateTimeField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).build();
    protected Field batchIdIn = Field.builder().name(batchIdInField).type(FieldType.of(DataType.INT, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field batchIdOut = Field.builder().name(batchIdOutField).type(FieldType.of(DataType.INT, Optional.empty(), Optional.empty())).build();
    protected Field batchTimeIn = Field.builder().name(batchTimeInField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).primaryKey(true).build();
    protected Field batchTimeOut = Field.builder().name(batchTimeOutField).type(FieldType.of(DataType.DATETIME, Optional.empty(), Optional.empty())).build();
    protected Field deleteIndicator = Field.builder().name(deleteIndicatorField).type(FieldType.of(DataType.VARCHAR, Optional.empty(), Optional.empty())).build();
    protected Field deleteIndicatorBoolean = Field.builder().name(deleteIndicatorField).type(FieldType.of(DataType.BOOLEAN, Optional.empty(), Optional.empty())).build();

    protected List<DataSplitRange> dataSplitRangesOneToTwo = new ArrayList<DataSplitRange>()
    {{
        add(DataSplitRange.of(1, 1));
        add(DataSplitRange.of(2, 2));
    }};

    protected List<DataSplitRange> dataSplitRanges = new ArrayList<DataSplitRange>()
    {{
        add(DataSplitRange.of(2, 5));
        add(DataSplitRange.of(6, 7));
        add(DataSplitRange.of(8, 10));
        add(DataSplitRange.of(11, 14));
    }};

    protected SchemaDefinition baseTableSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .build();

    protected SchemaDefinition baseTableShortenedSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .build();

    protected SchemaDefinition mainTableSchemaWithBatchIdAndTime = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(batchIdIn)
        .addFields(batchIdOut)
        .addFields(batchTimeInNonPrimary)
        .addFields(batchTimeOut)
        .build();

    protected SchemaDefinition mainTableSchemaWithBatchIdInNotPrimary = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(batchIdInNonPrimary)
        .addFields(batchIdOut)
        .addFields(batchTimeIn)
        .addFields(batchTimeOut)
        .build();

    protected SchemaDefinition mainTableBatchIdBasedSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(batchIdIn)
        .addFields(batchIdOut)
        .build();

    protected SchemaDefinition mainTableBatchIdAndVersionBasedSchema = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(bizDate)
            .addFields(digest)
            .addFields(version)
            .addFields(batchIdIn)
            .addFields(batchIdOut)
            .build();

    protected SchemaDefinition mainTableBatchIdBasedSchemaWithBatchIdInNotPrimary = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(batchIdInNonPrimary)
        .addFields(batchIdOut)
        .build();

    protected SchemaDefinition mainTableTimeBasedSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(batchTimeIn)
        .addFields(batchTimeOut)
        .build();

    protected SchemaDefinition mainTableTimeBasedSchemaWithBatchTimeInNotPrimary = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(batchTimeInNonPrimary)
        .addFields(batchTimeOut)
        .build();

    protected SchemaDefinition baseTableSchemaWithDigest = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .build();

    protected SchemaDefinition baseTableSchemaWithDigestAndVersion = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(bizDate)
            .addFields(digest)
            .addFields(version)
            .build();

    protected SchemaDefinition baseTableSchemaWithNoPrimaryKeys = SchemaDefinition.builder()
        .addFields(idNonPrimary)
        .addFields(nameNonPrimary)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .build();

    protected SchemaDefinition baseTableSchemaWithNoPrimaryKeysAndNoDigest = SchemaDefinition.builder()
        .addFields(idNonPrimary)
        .addFields(nameNonPrimary)
        .addFields(amount)
        .addFields(bizDate)
        .build();

    protected SchemaDefinition baseTableSchemaWithAuditAndNoPrimaryKeys = SchemaDefinition.builder()
            .addFields(idNonPrimary)
            .addFields(nameNonPrimary)
            .addFields(amount)
            .addFields(bizDate)
            .addFields(digest)
            .addFields(batchUpdateTimeNonPK)
            .build();

    protected SchemaDefinition baseTableSchemaWithDigestAndDataSplit = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(dataSplit)
        .build();

    protected SchemaDefinition baseTableSchemaWithDigestAndUpdateBatchTimeField = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(batchUpdateTime)
        .build();

    protected SchemaDefinition baseTableSchemaWithUpdateBatchTimeField = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(bizDate)
            .addFields(batchUpdateTime)
            .build();

    protected SchemaDefinition stagingTableSchemaWithLimitedColumns = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(digest)
        .build();

    protected SchemaDefinition stagingTableSchemaWithDeleteIndicator = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(deleteIndicator)
        .build();

    protected SchemaDefinition stagingTableSchemaWithBooleanDeleteIndicator = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(bizDate)
        .addFields(digest)
        .addFields(deleteIndicatorBoolean)
        .build();

    protected SchemaDefinition bitemporalMainTableSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(digest)
        .addFields(batchIdIn)
        .addFields(batchIdOut)
        .addFields(validityFromTarget)
        .addFields(validityThroughTarget)
        .build();

    protected SchemaDefinition bitemporalMainTableSchemaWithVersionBatchIdAndTime = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(digest)
            .addFields(version)
            .addFields(batchIdIn)
            .addFields(batchIdOut)
            .addFields(batchTimeInNonPrimary)
            .addFields(batchTimeOut)
            .addFields(validityFromTarget)
            .addFields(validityThroughTarget)
            .build();

    protected SchemaDefinition bitemporalMainTableSchemaWithVersionBatchDateTime = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(digest)
            .addFields(version)
            .addFields(batchTimeIn)
            .addFields(batchTimeOut)
            .addFields(validityFromTarget)
            .addFields(validityThroughTarget)
            .build();

    protected SchemaDefinition bitemporalFromOnlyMainTableSchema = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(digest)
            .addFields(batchIdIn)
            .addFields(batchIdOut)
            .addFields(validityFromTarget)
            .addFields(validityThroughTarget)
            .build();

    protected SchemaDefinition bitemporalFromOnlyMainTableWithVersionSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(digest)
        .addFields(version)
        .addFields(batchIdIn)
        .addFields(batchIdOut)
        .addFields(validityFromTarget)
        .addFields(validityThroughTarget)
        .build();

    protected SchemaDefinition bitemporalFromOnlyMainTableBatchIdAndTimeBasedSchema = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(digest)
            .addFields(batchIdIn)
            .addFields(batchIdOut)
            .addFields(batchTimeInNonPrimary)
            .addFields(batchTimeOut)
            .addFields(validityFromTarget)
            .addFields(validityThroughTarget)
            .build();

    protected SchemaDefinition bitemporalFromOnlyMainTableDateTimeBasedSchema = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(digest)
            .addFields(batchTimeIn)
            .addFields(batchTimeOut)
            .addFields(validityFromTarget)
            .addFields(validityThroughTarget)
            .build();

    protected SchemaDefinition bitemporalStagingTableSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(validityFromReference)
        .addFields(validityThroughReference)
        .addFields(digest)
        .build();

    protected SchemaDefinition bitemporalStagingTableSchemaWithVersionWithDataSplit = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(validityFromReference)
            .addFields(validityThroughReference)
            .addFields(digest)
            .addFields(version)
            .addFields(dataSplit)
            .build();

    protected SchemaDefinition bitemporalStagingTableSchemaWithDeleteIndicator = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(validityFromReference)
        .addFields(validityThroughReference)
        .addFields(digest)
        .addFields(deleteIndicator)
        .build();

    protected SchemaDefinition bitemporalStagingTableSchemaWithDeleteIndicatorVersionAndDataSplit = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(validityFromReference)
            .addFields(validityThroughReference)
            .addFields(digest)
            .addFields(version)
            .addFields(dataSplit)
            .addFields(deleteIndicator)
            .build();

    protected SchemaDefinition bitemporalFromOnlyStagingTableSchema = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(validityFromReference)
            .addFields(digest)
            .build();

    protected SchemaDefinition bitemporalFromOnlyStagingTableSchemaWithVersionWithDataSplit = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(validityFromReference)
            .addFields(digest)
            .addFields(version)
            .addFields(dataSplit)
            .build();

    protected SchemaDefinition bitemporalFromOnlyStagingTableSchemaWithDeleteIndicator = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(validityFromReference)
            .addFields(digest)
            .addFields(deleteIndicator)
            .build();

    protected SchemaDefinition bitemporalFromOnlyStagingTableSchemaWithDeleteIndicatorWithVersionWithDataSplit = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(validityFromReference)
            .addFields(digest)
            .addFields(version)
            .addFields(deleteIndicator)
            .addFields(dataSplit)
            .build();

    protected SchemaDefinition bitemporalFromOnlyTempTableSchema = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(digest)
            .addFields(batchIdIn)
            .addFields(batchIdOut)
            .addFields(validityFromTarget)
            .addFields(validityThroughTarget)
            .build();

    protected SchemaDefinition bitemporalFromOnlyTempTableWithVersionSchema = SchemaDefinition.builder()
        .addFields(id)
        .addFields(name)
        .addFields(amount)
        .addFields(digest)
        .addFields(version)
        .addFields(batchIdIn)
        .addFields(batchIdOut)
        .addFields(validityFromTarget)
        .addFields(validityThroughTarget)
        .build();

    protected SchemaDefinition bitemporalFromOnlyTempTableWithDeleteIndicatorSchema = SchemaDefinition.builder()
            .addFields(id)
            .addFields(name)
            .addFields(amount)
            .addFields(digest)
            .addFields(batchIdIn)
            .addFields(batchIdOut)
            .addFields(validityFromTarget)
            .addFields(validityThroughTarget)
            .addFields(deleteIndicator)
            .build();

    public void assertIfListsAreSameIgnoringOrder(List<String> first, List<String> second)
    {
        assertTrue(first.size() == second.size() &&
            first.stream().sorted().collect(Collectors.toList())
                .equals(second.stream().sorted().collect(Collectors.toList())));
    }

    private static final String SINGLE_QUOTE = "'";

    protected String enrichSqlWithDataSplits(String sql, DataSplitRange dataSplitRange)
    {
        return sql
                .replace(SINGLE_QUOTE + LogicalPlanUtils.DATA_SPLIT_LOWER_BOUND_PLACEHOLDER + SINGLE_QUOTE, String.valueOf(dataSplitRange.lowerBound()))
                .replace(SINGLE_QUOTE + LogicalPlanUtils.DATA_SPLIT_UPPER_BOUND_PLACEHOLDER + SINGLE_QUOTE, String.valueOf(dataSplitRange.upperBound()));
    }


    // All the main table and staging table

    protected Dataset mainTableWithBaseSchema = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(baseTableSchema)
            .build();

    protected Dataset stagingTableWithBaseSchema = DatasetDefinition.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchema)
            .build();
    protected Dataset mainTableWithNoPrimaryKeys = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(baseTableSchemaWithNoPrimaryKeys)
            .build();

    protected Dataset mainTableWithNoFields = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(SchemaDefinition.builder().build())
            .build();

    protected Dataset stagingTableWithNoPrimaryKeys = DatasetDefinition.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchemaWithNoPrimaryKeys)
            .build();

    protected Dataset stagingTableWithNoPrimaryKeysAndNoDigest = DatasetDefinition.builder()
        .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
        .schema(baseTableSchemaWithNoPrimaryKeysAndNoDigest)
        .build();

    protected Dataset mainTableWithNoPrimaryKeysHavingAuditField = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(baseTableSchemaWithAuditAndNoPrimaryKeys)
            .build();

    protected Dataset mainTableWithBaseSchemaAndDigest = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(baseTableSchemaWithDigest)
            .build();

    protected Dataset stagingTableWithBaseSchemaAndDigest = DatasetDefinition.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchemaWithDigest)
            .build();

    protected Dataset stagingTableWithFilter = DerivedDataset.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchemaWithDigest)
            .addDatasetFilters(DatasetFilter.of(batchIdInField, FilterType.GREATER_THAN, 5L))
            .build();

    protected Dataset stagingTableWithFilterAndVersion = DerivedDataset.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchemaWithDigestAndVersion)
            .addDatasetFilters(DatasetFilter.of(batchIdInField, FilterType.GREATER_THAN, 5L))
            .build();

    protected Dataset stagingTableWithVersion = DatasetDefinition.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchemaWithDigestAndVersion)
            .build();

    protected Dataset stagingTableWithVersionAndSnapshotId = DerivedDataset.builder()
        .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
        .schema(baseTableSchemaWithDigestAndVersion)
        .addDatasetFilters(DatasetFilter.of(snapshotIdField, FilterType.GREATER_THAN, 18972L))
        .build();

    protected Dataset mainTableWithVersion = DatasetDefinition.builder()
        .database(mainDbName).name(mainTableName).alias(mainTableAlias)
        .schema(baseTableSchemaWithDigestAndVersion)
        .build();

    protected Dataset stagingTableWithFilters = DerivedDataset.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchemaWithDigest)
            .addDatasetFilters(DatasetFilter.of("biz_date", FilterType.GREATER_THAN, "2020-01-01"))
            .addDatasetFilters(DatasetFilter.of("biz_date", FilterType.LESS_THAN, "2020-01-03"))
            .build();

    protected Dataset filteredStagingTable = FilteredDataset.builder()
        .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
        .schema(baseTableSchemaWithDigest)
        .filter(GreaterThan.of(FieldValue.builder()
            .fieldName(batchIdInField)
            .datasetRefAlias(stagingTableAlias)
            .build(), NumericalValue.of(5L)))
        .build();

    protected Dataset filteredStagingTableWithVersion = FilteredDataset.builder()
        .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
        .schema(baseTableSchemaWithDigestAndVersion)
        .filter(GreaterThan.of(FieldValue.builder()
            .fieldName(batchIdInField)
            .datasetRefAlias(stagingTableAlias)
            .build(), NumericalValue.of(5L)))
        .build();

    protected Dataset filteredStagingTableWithComplexFilter = FilteredDataset.builder()
        .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
        .schema(baseTableSchemaWithDigest)
        .filter(Or.builder()
            .addConditions(GreaterThan.of(FieldValue.builder().fieldName(bizDateField).datasetRefAlias(stagingTableAlias).build(), StringValue.of("2020-01-10")))
            .addConditions(And.builder()
                .addConditions(GreaterThan.of(FieldValue.builder().fieldName(bizDateField).datasetRefAlias(stagingTableAlias).build(), StringValue.of("2020-01-01")))
                .addConditions(LessThan.of(FieldValue.builder().fieldName(bizDateField).datasetRefAlias(stagingTableAlias).build(), StringValue.of("2020-01-05")))
                .build())
            .build())
        .build();

    protected Dataset stagingTableWithBaseSchemaAndDigestAndDeleteIndicator = DatasetDefinition.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(stagingTableSchemaWithDeleteIndicator)
            .build();

    protected Dataset mainTableWithBaseSchemaHavingDigestAndAuditField = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(baseTableSchemaWithDigestAndUpdateBatchTimeField)
            .build();

    protected Dataset mainTableWithBaseSchemaHavingAuditField = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(baseTableSchemaWithUpdateBatchTimeField)
            .build();

    protected Dataset stagingTableWithBaseSchemaHavingDigestAndDataSplit = DatasetDefinition.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(baseTableSchemaWithDigestAndDataSplit)
            .build();

    protected Dataset mainTableWithBatchIdBasedSchema = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(mainTableBatchIdBasedSchema)
            .build();


    protected Dataset mainTableWithBatchIdAndVersionBasedSchema = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(mainTableBatchIdAndVersionBasedSchema)
            .build();

    protected Dataset stagingTableWithDeleteIndicator = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(stagingTableSchemaWithDeleteIndicator)
            .build();

    protected Dataset stagingTableWithBooleanDeleteIndicator = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(stagingTableSchemaWithBooleanDeleteIndicator)
            .build();

    protected Dataset mainTableWithBatchIdAndTime = DatasetDefinition.builder()
        .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(mainTableSchemaWithBatchIdAndTime)
            .build();

    protected Dataset mainTableWithDateTime = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(mainTableTimeBasedSchema)
            .build();

    protected Dataset mainTableWithBitemporalSchema = DatasetDefinition.builder()
        .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(bitemporalMainTableSchema)
            .build();

    protected Dataset mainTableWithBitemporalSchemaWithVersionBatchDateTime = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(bitemporalMainTableSchemaWithVersionBatchDateTime)
            .build();

    protected Dataset stagingTableWithBitemporalSchema = DatasetDefinition.builder()
        .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(bitemporalStagingTableSchema)
            .build();

    protected Dataset stagingTableWithBitemporalSchemaWithDeleteIndicator = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(bitemporalStagingTableSchemaWithDeleteIndicator)
            .build();

    protected Dataset stagingTableWithBitemporalSchemaWithDeleteIndicatorVersionAndDataSplit = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(bitemporalStagingTableSchemaWithDeleteIndicatorVersionAndDataSplit)
            .build();

    protected Dataset mainTableWithBitemporalSchemaWithVersionBatchIdAndTime = DatasetDefinition.builder()
            .database(mainDbName).name(mainTableName).alias(mainTableAlias)
            .schema(bitemporalMainTableSchemaWithVersionBatchIdAndTime)
            .build();

    protected Dataset stagingTableWithBitemporalSchemaWithVersionWithDataSplit = DatasetDefinition.builder()
            .database(stagingDbName).name(stagingTableName).alias(stagingTableAlias)
            .schema(bitemporalStagingTableSchemaWithVersionWithDataSplit)
            .build();

    protected DatasetDefinition mainTableWithBitemporalFromOnlySchema = DatasetDefinition.builder()
            .database(mainDbName)
            .name(mainTableName)
            .alias(mainTableAlias)
            .schema(bitemporalFromOnlyMainTableSchema)
            .build();

    protected DatasetDefinition mainTableWithBitemporalFromOnlyWithVersionSchema = DatasetDefinition.builder()
        .database(mainDbName)
        .name(mainTableName)
        .alias(mainTableAlias)
        .schema(bitemporalFromOnlyMainTableWithVersionSchema)
        .build();

    protected DatasetDefinition mainTableWithBitemporalFromOnlyWithBatchIdAndTimeBasedSchema = DatasetDefinition.builder()
            .database(mainDbName)
            .name(mainTableName)
            .alias(mainTableAlias)
            .schema(bitemporalFromOnlyMainTableBatchIdAndTimeBasedSchema)
            .build();

    protected DatasetDefinition tempTableWithBitemporalFromOnlyWithBatchIdAndTimeBasedSchema = DatasetDefinition.builder()
            .database(tempDbName)
            .name(tempTableName)
            .alias(tempTableAlias)
            .schema(bitemporalFromOnlyMainTableBatchIdAndTimeBasedSchema)
            .build();

    protected DatasetDefinition mainTableWithBitemporalFromOnlyWithDateTimeBasedSchema = DatasetDefinition.builder()
            .database(mainDbName)
            .name(mainTableName)
            .alias(mainTableAlias)
            .schema(bitemporalFromOnlyMainTableDateTimeBasedSchema)
            .build();

    protected DatasetDefinition tempTableWithBitemporalFromOnlyWithDateTimeBasedSchema = DatasetDefinition.builder()
            .database(tempDbName)
            .name(tempTableName)
            .alias(tempTableAlias)
            .schema(bitemporalFromOnlyMainTableDateTimeBasedSchema)
            .build();

    protected DatasetDefinition stagingTableWithBitemporalFromOnlySchema = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(bitemporalFromOnlyStagingTableSchema)
            .build();

    protected DatasetDefinition stagingTableWithBitemporalFromOnlySchemaWithVersionWithDataSplit = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(bitemporalFromOnlyStagingTableSchemaWithVersionWithDataSplit)
            .build();

    protected DatasetDefinition tempTableWithBitemporalFromOnlySchema = DatasetDefinition.builder()
            .database(tempDbName)
            .name(tempTableName)
            .alias(tempTableAlias)
            .schema(bitemporalFromOnlyTempTableSchema)
            .build();

    protected DatasetDefinition tempTableWithBitemporalFromOnlyWithVersionSchema = DatasetDefinition.builder()
        .database(tempDbName)
        .name(tempTableName)
        .alias(tempTableAlias)
        .schema(bitemporalFromOnlyTempTableWithVersionSchema)
        .build();

    protected DatasetDefinition stagingTableWithBitemporalFromOnlySchemaWithDeleteInd = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(bitemporalFromOnlyStagingTableSchemaWithDeleteIndicator)
            .build();

    protected DatasetDefinition stagingTableWithBitemporalFromOnlySchemaWithDeleteIndWithVersionWithDataSplit = DatasetDefinition.builder()
            .database(stagingDbName)
            .name(stagingTableName)
            .alias(stagingTableAlias)
            .schema(bitemporalFromOnlyStagingTableSchemaWithDeleteIndicatorWithVersionWithDataSplit)
            .build();

    protected DatasetDefinition stagingTableBitemporalWithoutDuplicates = DatasetDefinition.builder()
            .database(stagingWithoutDuplicatesDbName)
            .name(stagingTableWithoutDuplicatesName)
            .alias(stagingTableWithoutDuplicatesAlias)
            .schema(bitemporalFromOnlyStagingTableSchema)
            .build();
    protected DatasetDefinition tempTableWithDeleteIndicator = DatasetDefinition.builder()
            .database(tempWithDeleteIndicatorDbName)
            .name(tempWithDeleteIndicatorTableName)
            .alias(tempWithDeleteIndicatorTableAlias)
            .schema(bitemporalFromOnlyTempTableWithDeleteIndicatorSchema)
            .build();

    protected void verifyStats(GeneratorResult operations, String incomingRecordCount, String rowsUpdated, String rowsDeleted, String rowsInserted, String rowsTerminated)
    {
        Assertions.assertEquals(incomingRecordCount, operations.postIngestStatisticsSql().get(StatisticName.INCOMING_RECORD_COUNT));
        Assertions.assertEquals(rowsUpdated, operations.postIngestStatisticsSql().get(StatisticName.ROWS_UPDATED));
        Assertions.assertEquals(rowsDeleted, operations.postIngestStatisticsSql().get(StatisticName.ROWS_DELETED));
        Assertions.assertEquals(rowsInserted, operations.postIngestStatisticsSql().get(StatisticName.ROWS_INSERTED));
        Assertions.assertEquals(rowsTerminated, operations.postIngestStatisticsSql().get(StatisticName.ROWS_TERMINATED));
    }

    protected Dataset getMainDatasetWithDbAndSchemaBothSet(SchemaDefinition schemaDefinition)
    {
        return  DatasetDefinition.builder()
                .database(mainDbName)
                .group(schema)
                .name(mainTableName)
                .alias(mainTableAlias)
                .schema(schemaDefinition)
                .build();
    }

    protected Dataset getStagingDatasetWithDbAndSchemaBothSet(SchemaDefinition schemaDefinition)
    {
        return  DatasetDefinition.builder()
                .database(mainDbName)
                .group(schema)
                .name(stagingTableName)
                .alias(stagingTableAlias)
                .schema(schemaDefinition)
                .build();
    }

    protected Dataset getMainDatasetWithDbAndSchemaBothNotSet(SchemaDefinition schemaDefinition)
    {
        return  DatasetDefinition.builder()
                .name(mainTableName)
                .alias(mainTableAlias)
                .schema(schemaDefinition)
                .build();
    }

    protected Dataset getStagingDatasetWithDbAndSchemaBothNotSet(SchemaDefinition schemaDefinition)
    {
        return  DatasetDefinition.builder()
                .name(stagingTableName)
                .alias(stagingTableAlias)
                .schema(schemaDefinition)
                .build();
    }

    protected Dataset getMainDatasetWithOnlySchemaSet(SchemaDefinition schemaDefinition)
    {
        return  DatasetDefinition.builder()
                .group(schema)
                .name(mainTableName)
                .alias(mainTableAlias)
                .schema(schemaDefinition)
                .build();
    }

    protected Dataset getStagingDatasetWithOnlySchemaSet(SchemaDefinition schemaDefinition)
    {
        return  DatasetDefinition.builder()
                .group(schema)
                .name(stagingTableName)
                .alias(stagingTableAlias)
                .schema(schemaDefinition)
                .build();
    }

    protected String getExpectedCleanupSql(String fullName, String alias)
    {
        return String.format("DELETE FROM %s as %s", fullName, alias);
    }

    public static void assertDerivedMainDataset(TestScenario scenario)
    {
        Dataset stagingDataset = scenario.getDatasets().stagingDataset();
        Dataset expectedMainDataset = scenario.getDatasets().mainDataset();
        Dataset derivedMainDataset = scenario.getIngestMode().accept(new DeriveMainDatasetSchemaFromStaging(expectedMainDataset, stagingDataset));
        Assertions.assertEquals(expectedMainDataset, derivedMainDataset);
    }
}
