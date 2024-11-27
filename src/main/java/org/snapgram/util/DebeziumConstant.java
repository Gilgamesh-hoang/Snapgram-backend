package org.snapgram.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DebeziumConstant {
    public final String DEBEZIUM_UPDATE = "u";
    public final String DEBEZIUM_CREATE = "c";
    public final String DEBEZIUM_DELETE = "d";
    public final String DEBEZIUM_READ = "r";
}
