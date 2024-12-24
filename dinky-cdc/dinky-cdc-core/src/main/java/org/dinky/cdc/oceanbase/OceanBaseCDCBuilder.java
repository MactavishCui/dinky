package org.dinky.cdc.oceanbase;


import alluxio.shaded.client.org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cdc.connectors.base.options.StartupOptions;
import org.apache.flink.cdc.connectors.oceanbase.OceanBaseSource;
import org.apache.flink.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.dinky.assertion.Asserts;
import org.dinky.cdc.AbstractCDCBuilder;
import org.dinky.cdc.CDCBuilder;
import org.dinky.data.model.FlinkCDCConfig;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class OceanBaseCDCBuilder extends AbstractCDCBuilder {
    public static final String KEY_WORD = "oceanbase-cdc";
    private static final String METADATA_TYPE = "Oceanbase";

    public OceanBaseCDCBuilder() {
    }

    public OceanBaseCDCBuilder(FlinkCDCConfig config) {
        super(config);
    }

    @Override
    public String getHandle() {
        return KEY_WORD;
    }

    @Override
    public CDCBuilder create(FlinkCDCConfig config) {
        return new OceanBaseCDCBuilder(config);
    }

    @Override
    public DataStreamSource<String> build(StreamExecutionEnvironment env) {
        Map<String, String> source = config.getSource();
        String tenantName = source.get("tenant-name");
        String databaseName = source.get("database-name");
        String scanStartupTimestamp = source.get("scan.startup.timestamp");
        String connectTimeout = source.get("connect.timeout");
        String serverTimeZone = source.get("server-time-zone");
        String logProxyHost = source.get("logproxy.host");
        String logProxyPort = source.get("logproxy.port");
        String logProxyClientId = source.get("logproxy.client.id");
        String rsList = source.get("rootserver-list");
        String configUrl = source.get("config-url");
        String workingMode = source.get("working-mode");
        String compatibleMode = source.get("compatible-mode");
        String jdbcDriver = source.get("jdbc.driver");

        // host name, port, username and password are required
        OceanBaseSource.Builder<String> sourceBuilder = OceanBaseSource.<String>builder()
                .hostname(config.getHostname())
                .port(config.getPort())
                .username(config.getUsername())
                .password(config.getPassword());

        // optional configurations
        if (Asserts.isNotNullString(tenantName)) {
            sourceBuilder.tenantName(tenantName);
        }
        if (Asserts.isNotNullString(databaseName)) {
            sourceBuilder.databaseName(databaseName);
        }
        if (Asserts.isNotNullString(serverTimeZone)) {
            sourceBuilder.serverTimeZone(serverTimeZone);
        }
        if (Asserts.isNotNullString(connectTimeout)) {
            sourceBuilder.connectTimeout(Duration.ofMillis(Long.parseLong(connectTimeout)));
        }
        if (Asserts.isNotNullString(compatibleMode)) {
            sourceBuilder.compatibleMode(compatibleMode);
        }
        if (Asserts.isNotNullString(jdbcDriver)) {
            sourceBuilder.jdbcDriver(jdbcDriver);
        }
        if (Asserts.isNotNullString(workingMode)) {
            sourceBuilder.workingMode(workingMode);
        }
        if (Asserts.isNotNullString(logProxyHost)) {
            sourceBuilder.logProxyHost(logProxyHost);
        }
        if (Asserts.isNotNullString(logProxyPort)) {
            sourceBuilder.logProxyPort(Integer.valueOf(logProxyPort));
        }
        if (Asserts.isNotNullString(logProxyClientId)) {
            sourceBuilder.logProxyClientId(logProxyClientId);
        }
        if (Asserts.isNotNullString(rsList)) {
            sourceBuilder.rsList(rsList);
        }
        if (Asserts.isNotNullString(configUrl)) {
            sourceBuilder.configUrl(configUrl);
        }
        // deserializer
        sourceBuilder.deserializer(new JsonDebeziumDeserializationSchema());
        Properties properties = new Properties();
        config.getDebezium().forEach((key, value) -> {
            if (Asserts.isNotNullString(key) && Asserts.isNotNullString(value)) {
                properties.setProperty(key, value);
            }
        });
        sourceBuilder.debeziumProperties(properties);
        // jdbcProperties
        Properties jdbcProperties = new Properties();
        config.getJdbc().forEach((key, value) -> {
            if (Asserts.isNotNullString(key) && Asserts.isNotNullString(value)) {
                jdbcProperties.setProperty(key, value);
            }
        });
        sourceBuilder.jdbcProperties(jdbcProperties);
        // odcbc properties
        Properties odcbcProperties = new Properties();
        String odcbcPrefix = "obcdc.properties.";
        for (Map.Entry<String, String> entry : source.entrySet()) {
            String key = entry.getKey();
            if (entry.getKey().startsWith(odcbcPrefix)) {
                key = key.replaceFirst(odcbcPrefix, "");
                odcbcProperties.setProperty(key, entry.getValue());
            }
        }
        sourceBuilder.obcdcProperties(odcbcProperties);

        List<String> schemaTableNameList = config.getSchemaTableNameList();
        if (Asserts.isNotNullCollection(schemaTableNameList)) {
            sourceBuilder.tableList(StringUtils.join(schemaTableNameList, ","));
        } else {
            sourceBuilder.tableList("");
        }
        if (Asserts.isNotNullString(config.getStartupMode())) {
            switch (config.getStartupMode().toLowerCase()) {
                case "initial":
                    sourceBuilder.startupOptions(StartupOptions.initial());
                    break;
                case "latest-offset":
                    sourceBuilder.startupOptions(StartupOptions.latest());
                    break;
                case "timestamp":
                    if (Asserts.isNotNullString(scanStartupTimestamp)) {
                        sourceBuilder.startupOptions(StartupOptions.timestamp(Long.parseLong(scanStartupTimestamp)));
                    } else {
                        throw new RuntimeException("No timestamp parameter specified.");
                    }
                case "snapshot":
                    sourceBuilder.startupOptions(StartupOptions.snapshot());
                default:
            }
        } else {
            sourceBuilder.startupOptions(StartupOptions.latest());
        }
        return env.addSource(sourceBuilder.build(), "Ocean Base CDC Source");
    }

    @Override
    public String getSchemaFieldName() {
        return "db";
    }

    @Override
    public String getSchema() {
        return config.getDatabase();
    }

    @Override
    protected String getMetadataType() {
        return METADATA_TYPE;
    }

    @Override
    protected String generateUrl(String schema) {
        return String.format(
                "jdbc:oceanbase://%s:%d/%s%s",
                config.getHostname(), config.getPort(), schema, composeJdbcProperties(config.getJdbc()));
    }

    private String composeJdbcProperties(Map<String, String> jdbcProperties) {
        if (jdbcProperties == null || jdbcProperties.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('?');
        jdbcProperties.forEach((k, v) -> {
            sb.append(k);
            sb.append("=");
            sb.append(v);
            sb.append("&");
        });
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
