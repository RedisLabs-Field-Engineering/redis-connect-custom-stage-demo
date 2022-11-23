package com.redis.connect.customstage;

import java.lang.management.ManagementFactory;
import com.redis.connect.customstage.impl.ClobToJSON;
import com.redis.connect.customstage.impl.SplunkForwardHECRequestCustomStage;
import com.redis.connect.customstage.impl.ToUpperCase;
import com.redis.connect.dto.JobPipelineStageDTO;
import com.redis.connect.exception.ValidationException;
import com.redis.connect.pipeline.event.handler.ChangeEventHandler;
import com.redis.connect.pipeline.event.handler.ChangeEventHandlerFactory;
import java.util.HashSet;
import java.util.Set;

public class CustomChangeEventHandlerFactory implements ChangeEventHandlerFactory {

    private final String instanceId = ManagementFactory.getRuntimeMXBean().getName();

    private static final String TO_UPPER_CASE = "TO_UPPER_CASE";
    private static final String CLOB_TO_JSON = "CLOB_TO_JSON";
    private static final String FORWARD_HEC_REQUEST = "FORWARD_HEC_REQUEST";

    private static final Set<String> supportedChangeEventHandlers = new HashSet<>();

    static {
        supportedChangeEventHandlers.add(TO_UPPER_CASE);
        supportedChangeEventHandlers.add(CLOB_TO_JSON);
        supportedChangeEventHandlers.add(FORWARD_HEC_REQUEST);
    }

    @Override
    public ChangeEventHandler getInstance(String jobId, String jobType, JobPipelineStageDTO jobPipelineStage) throws Exception {

        ChangeEventHandler changeEventHandler;

        switch (jobPipelineStage.getStageName()) {
            case TO_UPPER_CASE:
                changeEventHandler = new ToUpperCase(jobId, jobType, jobPipelineStage);
                break;
            case CLOB_TO_JSON:
                changeEventHandler = new ClobToJSON(jobId, jobType, jobPipelineStage);
                break;
            case FORWARD_HEC_REQUEST:
                changeEventHandler = new SplunkForwardHECRequestCustomStage(jobId, jobType, jobPipelineStage);
                break;
            default: {
                throw new ValidationException("Instance: " + instanceId + " failed to load change event handler for " +
                        " JobId: " + jobId + " due to an invalid job pipeline Stage: " + jobPipelineStage.getStageName());
            }
        }

        return changeEventHandler;
    }

    @Override
    public String getType() {
        return "CUSTOM";
    }

    @Override
    public boolean contains(String stageName) {
        return supportedChangeEventHandlers.contains(stageName);
    }

}