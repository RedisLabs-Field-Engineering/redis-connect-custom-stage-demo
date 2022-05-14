package com.redis.connect.customstage.impl;

import com.redis.connect.dto.ChangeEventDTO;
import com.redis.connect.dto.JobPipelineStageDTO;
import com.redis.connect.pipeline.event.handler.ChangeEventHandler;
import com.redis.connect.utils.ConnectConstants;

import java.lang.management.ManagementFactory;
import java.util.Map;

/**
 * This is an example of writing a custom transformation
 * i.e. not already built with Redis Connect framework. For example, a custom transformation you need
 * to apply before writing the changes to Redis Enterprise target.
 * Pass any 2 columns with STRING data type to convert them to UPPER CASE
 * e.g. -Dcol1=fname -Dcol2=lname
 * <p>
 * NOTE: Any CustomStage Classes must implement the ChangeEventHandler interface as this is the source of
 * all the changes coming to Redis Connect framework.
 */
public class CustomStage implements ChangeEventHandler<Map<String, Object>> {

    private final String instanceId = ManagementFactory.getRuntimeMXBean().getName();

    protected String jobId;
    protected String jobType;
    protected JobPipelineStageDTO jobPipelineStage;

    public CustomStage(String jobId, String jobType, JobPipelineStageDTO jobPipelineStage) {
        this.jobId = jobId;
        this.jobType = jobType;
        this.jobPipelineStage = jobPipelineStage;
    }

    @Override
    public void onEvent(ChangeEventDTO<Map<String, Object>> changeEvent, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("-------------------------------------------Stage: CUSTOM Sequence: " + sequence);

        try {

            if (!changeEvent.isValid())
                System.out.println("Instance: " + instanceId + " received an invalid transition event payload for JobId: " + jobId);

            for (Map<String, Object> payload : changeEvent.getPayloads()) {

                String schemaAndTableName = payload.get(ConnectConstants.CHANGE_EVENT_SCHEMA) + "." + payload.get(ConnectConstants.CHANGE_EVENT_TABLE);

                Map<String, String> values = (Map<String, String>) payload.get(ConnectConstants.CHANGE_EVENT_VALUES);

                String operationType = (String) payload.get(ConnectConstants.CHANGE_EVENT_OPERATION_TYPE);

                System.out.println("CustomStage::onEvent Processor, " + "jobId: " + jobId + ", schemaAndTableName: " + schemaAndTableName + ", operationType: " + operationType);

                if (!values.isEmpty()) {

                    String col1Key = System.getProperty("col1", "fname");
                    String col2Key = System.getProperty("col2", "lname");
                    String col1Value = values.getOrDefault(System.getProperty("col1", "fname"), "fname");
                    String col2Value = values.getOrDefault(System.getProperty("col2", "lname"), "lname");

                    // Update the value(s) for col1Value coming from the source to upper case
                    if (col1Value != null) {
                        System.out.println("Original " + col1Key + ": " + col1Value);
                        values.put(System.getProperty("col1", "fname"), col1Value.toUpperCase());
                        System.out.println("Updated " + col1Key + ": " + values.get(System.getProperty("col1", "fname")));
                    }
                    // Update the value(s) for col2Value coming from the source to upper case
                    if (col2Value != null) {
                        System.out.println("Original " + col2Key + ": " + col2Value);
                        values.put(System.getProperty("col2", "lname"), col2Value.toUpperCase());
                        System.out.println("Updated " + col2Key + ": " + values.get(System.getProperty("col2", "lname")));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Instance: " + instanceId + "MESSAGE: " + e.getMessage() + "STACKTRACE: " + e);
        }
    }

}