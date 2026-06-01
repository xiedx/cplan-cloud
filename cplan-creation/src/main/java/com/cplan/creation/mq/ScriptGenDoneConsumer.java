package com.cplan.creation.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cplan.common.constant.MqTopicConstant;
import com.cplan.creation.entity.Outline;
import com.cplan.creation.entity.Project;
import com.cplan.creation.entity.Script;
import com.cplan.creation.entity.Storyboard;
import com.cplan.creation.entity.VideoTask;
import com.cplan.creation.mapper.OutlineMapper;
import com.cplan.creation.mapper.ProjectMapper;
import com.cplan.creation.mapper.ScriptMapper;
import com.cplan.creation.mapper.StoryboardMapper;
import com.cplan.creation.mapper.VideoTaskMapper;
import com.cplan.creation.statemachine.TaskEvent;
import com.cplan.creation.statemachine.VideoTaskStateMachine;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consumer that handles script-generation-done callback messages.
 * Saves the generated script, parses it into storyboards, and notifies progress.
 */
@Component
@RocketMQMessageListener(
        topic = MqTopicConstant.TOPIC_SCRIPT_GEN_DONE,
        consumerGroup = MqTopicConstant.GROUP_SCRIPT_GEN_DONE
)
public class ScriptGenDoneConsumer implements RocketMQListener<Map<String, Object>> {

    private static final Logger log = LoggerFactory.getLogger(ScriptGenDoneConsumer.class);

    /** Pattern to split script content into scenes by "===== 第X幕" delimiters. */
    private static final Pattern SCENE_PATTERN = Pattern.compile(
            "=====\\s*第.+幕[^=]*=====", Pattern.UNICODE_CHARACTER_CLASS
    );

    private final ScriptMapper scriptMapper;
    private final OutlineMapper outlineMapper;
    private final StoryboardMapper storyboardMapper;
    private final VideoTaskMapper videoTaskMapper;
    private final ProjectMapper projectMapper;
    private final VideoTaskStateMachine stateMachine;
    private final VideoTaskProducer videoTaskProducer;

    public ScriptGenDoneConsumer(ScriptMapper scriptMapper,
                                 OutlineMapper outlineMapper,
                                 StoryboardMapper storyboardMapper,
                                 VideoTaskMapper videoTaskMapper,
                                 ProjectMapper projectMapper,
                                 VideoTaskStateMachine stateMachine,
                                 VideoTaskProducer videoTaskProducer) {
        this.scriptMapper = scriptMapper;
        this.outlineMapper = outlineMapper;
        this.storyboardMapper = storyboardMapper;
        this.videoTaskMapper = videoTaskMapper;
        this.projectMapper = projectMapper;
        this.stateMachine = stateMachine;
        this.videoTaskProducer = videoTaskProducer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(Map<String, Object> message) {
        log.info("Received SCRIPT_GEN_DONE: {}", message);

        Long projectId = toLong(message.get("projectId"));
        Long outlineId = toLong(message.get("outlineId"));
        String status = (String) message.getOrDefault("status", "SUCCESS");
        String scriptContent = (String) message.get("scriptContent");
        String llmModel = (String) message.get("llmModel");
        String errorMessage = (String) message.get("errorMessage");

        if (projectId == null) {
            log.warn("SCRIPT_GEN_DONE message missing projectId");
            return;
        }

        if ("SUCCESS".equals(status)) {
            handleSuccess(projectId, outlineId, scriptContent, llmModel);
        } else {
            handleFailure(projectId, outlineId, errorMessage);
        }
    }

    private void handleSuccess(Long projectId, Long outlineId, String scriptContent, String llmModel) {
        // 1. Save the script
        Script script = Script.builder()
                .projectId(projectId)
                .outlineId(outlineId)
                .content(scriptContent)
                .llmModel(llmModel)
                .status(1) // COMPLETED
                .isDeleted(0)
                .build();
        scriptMapper.insert(script);

        // 2. Update outline status
        if (outlineId != null) {
            Outline outline = outlineMapper.selectById(outlineId);
            if (outline != null) {
                outline.setStatus(2); // COMPLETED
                outline.setEnrichedContent(scriptContent);
                outlineMapper.updateById(outline);
            }
        }

        // 3. Update the SCRIPT_GEN VideoTask to SUCCESS
        VideoTask scriptTask = videoTaskMapper.selectOne(
                new LambdaQueryWrapper<VideoTask>()
                        .eq(VideoTask::getProjectId, projectId)
                        .eq(VideoTask::getTaskType, "SCRIPT_GEN")
                        .orderByDesc(VideoTask::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (scriptTask != null) {
            stateMachine.transition(scriptTask, TaskEvent.SUCCESS);
            scriptTask.setFinishedAt(LocalDateTime.now());
            videoTaskMapper.updateById(scriptTask);
        }

        // 4. Parse script content into storyboards
        parseAndCreateStoryboards(projectId, script.getId(), scriptContent);

        // 5. Notify progress
        videoTaskProducer.sendNotify(MqTopicConstant.TAG_TASK_PROGRESS,
                Map.of("projectId", projectId,
                        "type", "SCRIPT_GEN",
                        "status", "SUCCESS",
                        "message", "剧本生成完成"));

        log.info("Script gen done processed: projectId={}, scriptId={}", projectId, script.getId());
    }

    private void handleFailure(Long projectId, Long outlineId, String errorMessage) {
        // Update the SCRIPT_GEN VideoTask to FAILED
        VideoTask scriptTask = videoTaskMapper.selectOne(
                new LambdaQueryWrapper<VideoTask>()
                        .eq(VideoTask::getProjectId, projectId)
                        .eq(VideoTask::getTaskType, "SCRIPT_GEN")
                        .orderByDesc(VideoTask::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (scriptTask != null) {
            stateMachine.transition(scriptTask, TaskEvent.FAIL);
            scriptTask.setErrorMessage(errorMessage);
            scriptTask.setFinishedAt(LocalDateTime.now());
            videoTaskMapper.updateById(scriptTask);
        }

        // Update outline status
        if (outlineId != null) {
            Outline outline = outlineMapper.selectById(outlineId);
            if (outline != null) {
                outline.setStatus(0); // Reset to PENDING
                outlineMapper.updateById(outline);
            }
        }

        // Update project status
        Project project = projectMapper.selectById(projectId);
        if (project != null) {
            project.setStatus(3); // FAILED
            projectMapper.updateById(project);
        }

        videoTaskProducer.sendNotify(MqTopicConstant.TAG_TASK_PROGRESS,
                Map.of("projectId", projectId,
                        "type", "SCRIPT_GEN",
                        "status", "FAILED",
                        "errorMessage", errorMessage != null ? errorMessage : ""));

        log.warn("Script gen failed: projectId={}, error={}", projectId, errorMessage);
    }

    /**
     * Parse the script content into individual storyboard scenes.
     * Splits by "===== 第X幕" delimiters and extracts scene description, dialogue, and video prompt.
     */
    private void parseAndCreateStoryboards(Long projectId, Long scriptId, String scriptContent) {
        if (scriptContent == null || scriptContent.isBlank()) {
            log.warn("Script content is empty for project {}", projectId);
            return;
        }

        // Split by scene delimiters
        String[] scenes = SCENE_PATTERN.split(scriptContent);
        // Find all scene titles
        java.util.List<String> sceneTitles = new java.util.ArrayList<>();
        Matcher matcher = SCENE_PATTERN.matcher(scriptContent);
        while (matcher.find()) {
            sceneTitles.add(matcher.group().trim());
        }

        int sequenceNo = 1;
        // Start from index 1 because split produces an empty first element before the first delimiter
        for (int i = 1; i < scenes.length && sequenceNo <= (scenes.length - 1); i++) {
            String sceneText = scenes[i].trim();
            if (sceneText.isBlank()) continue;

            String sceneDescription = extractField(sceneText, "场景描述");
            String dialogue = extractField(sceneText, "台词");
            String imagePrompt = "";
            String videoPrompt = extractField(sceneText, "画面提示");

            Storyboard storyboard = Storyboard.builder()
                    .projectId(projectId)
                    .scriptId(scriptId)
                    .sequenceNo(sequenceNo)
                    .sceneDescription(sceneDescription)
                    .dialogue(dialogue)
                    .imagePrompt(imagePrompt)
                    .videoPrompt(videoPrompt)
                    .status(0) // PENDING_REVIEW
                    .isDeleted(0)
                    .build();
            storyboardMapper.insert(storyboard);
            sequenceNo++;
        }

        log.info("Parsed {} storyboards for project {}", sequenceNo - 1, projectId);
    }

    /**
     * Extract a specific field value from scene text.
     * Format: "字段名：value" or "字段名:value"
     */
    private String extractField(String text, String fieldName) {
        Pattern pattern = Pattern.compile(fieldName + "[：:]\\s*(.+?)(?=\\n|$)", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
