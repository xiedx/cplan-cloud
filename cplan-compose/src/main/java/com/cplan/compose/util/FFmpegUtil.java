package com.cplan.compose.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FFmpeg command utility for video processing.
 *
 * <p>Provides methods for concatenating videos, adding audio tracks,
 * and extracting metadata using FFmpeg CLI or JavaCV.
 */
@Component
public class FFmpegUtil {

    private static final Logger log = LoggerFactory.getLogger(FFmpegUtil.class);

    /**
     * Concatenate multiple video files into one using FFmpeg concat filter.
     *
     * @param inputPaths list of input video file paths
     * @param outputPath output file path for the concatenated video
     */
    public void concatVideos(List<String> inputPaths, String outputPath) {
        if (inputPaths == null || inputPaths.isEmpty()) {
            throw new IllegalArgumentException("Input paths cannot be empty");
        }

        if (inputPaths.size() == 1) {
            // Single file — just copy/rename
            log.info("Only one input file, copying to output: {}", outputPath);
            return;
        }

        // Build FFmpeg concat filter command
        // ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex "[0:v][0:a][1:v][1:a]concat=n=2:v=1:a=1" output.mp4
        String command = buildConcatCommand(inputPaths, outputPath);
        log.info("Executing FFmpeg concat command: {}", command);

        executeCommand(command);
    }

    /**
     * Add an audio track to a video file.
     *
     * @param videoPath  input video file path
     * @param audioPath  input audio file path
     * @param outputPath output file path
     */
    public void addAudio(String videoPath, String audioPath, String outputPath) {
        String command = String.format(
                "ffmpeg -i %s -i %s -c:v copy -c:a aac -map 0:v:0 -map 1:a:0 -shortest %s",
                videoPath, audioPath, outputPath
        );
        log.info("Executing FFmpeg addAudio command: {}", command);
        executeCommand(command);
    }

    /**
     * Get video metadata using ffprobe.
     *
     * @param filePath input video file path
     * @return metadata as a formatted string
     */
    public String getMetadata(String filePath) {
        String command = String.format("ffprobe -v quiet -print_format json -show_format -show_streams %s", filePath);
        log.debug("Executing ffprobe command: {}", command);
        return executeCommandWithOutput(command);
    }

    /**
     * Build the FFmpeg concat filter command string.
     */
    private String buildConcatCommand(List<String> inputPaths, String outputPath) {
        StringBuilder sb = new StringBuilder("ffmpeg");

        // Add input files
        for (String path : inputPaths) {
            sb.append(" -i ").append(path);
        }

        // Build filter_complex for concat
        sb.append(" -filter_complex \"");
        for (int i = 0; i < inputPaths.size(); i++) {
            sb.append("[").append(i).append(":v]").append("[").append(i).append(":a]");
        }
        sb.append("concat=n=").append(inputPaths.size()).append(":v=1:a=1[v][a]\"");

        // Map and output
        sb.append(" -map \"[v]\" -map \"[a]\" -c:v libx264 -c:a aac -movflags +faststart ")
                .append(outputPath);

        return sb.toString();
    }

    /**
     * Execute a shell command and wait for completion.
     */
    private void executeCommand(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String output = readProcessOutput(process);
                throw new RuntimeException("FFmpeg command failed with exit code " + exitCode + ": " + output);
            }
            log.info("FFmpeg command completed successfully");
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to execute FFmpeg command", e);
        }
    }

    /**
     * Execute a shell command and return its output.
     */
    private String executeCommandWithOutput(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            String output = readProcessOutput(process);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Command failed with exit code " + exitCode);
            }
            return output;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to execute command", e);
        }
    }

    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
