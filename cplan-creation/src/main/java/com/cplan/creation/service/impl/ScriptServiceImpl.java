package com.cplan.creation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cplan.common.exception.BizException;
import com.cplan.common.result.ResultCode;
import com.cplan.creation.dto.ScriptVO;
import com.cplan.creation.entity.Script;
import com.cplan.creation.mapper.ScriptMapper;
import com.cplan.creation.service.ScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of ScriptService.
 */
@Service
public class ScriptServiceImpl implements ScriptService {

    private static final Logger log = LoggerFactory.getLogger(ScriptServiceImpl.class);

    private final ScriptMapper scriptMapper;

    public ScriptServiceImpl(ScriptMapper scriptMapper) {
        this.scriptMapper = scriptMapper;
    }

    @Override
    public ScriptVO getScript(Long projectId) {
        Script script = scriptMapper.selectOne(
                new LambdaQueryWrapper<Script>()
                        .eq(Script::getProjectId, projectId)
                        .orderByDesc(Script::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (script == null) {
            throw new BizException(ResultCode.SCRIPT_NOT_FOUND);
        }

        return ScriptVO.builder()
                .scriptId(script.getId())
                .content(script.getContent())
                .llmModel(script.getLlmModel())
                .status(script.getStatus())
                .build();
    }
}
