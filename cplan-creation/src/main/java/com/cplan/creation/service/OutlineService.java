package com.cplan.creation.service;

import com.cplan.creation.dto.OutlineVO;
import com.cplan.creation.dto.SubmitOutlineRequest;

/**
 * Outline service interface.
 */
public interface OutlineService {

    /**
     * Submit an outline for a project, which triggers async script generation.
     *
     * @param userId    the owner's user ID (from gateway header)
     * @param projectId the project ID
     * @param req       outline submission request
     * @return outline view object
     */
    OutlineVO submitOutline(Long userId, Long projectId, SubmitOutlineRequest req);
}
