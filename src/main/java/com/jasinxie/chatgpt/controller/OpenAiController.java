package com.jasinxie.chatgpt.controller;

import com.jasinxie.chatgpt.listener.OpenAiChatListener;
import com.jasinxie.chatgpt.listener.OpenAiCompletionListener;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.completions.Completion;
import com.unfbx.chatgpt.entity.completions.CompletionResponse;
import com.unfbx.chatgpt.exception.BaseException;
import com.unfbx.chatgpt.exception.CommonError;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RequestMapping("/v1")
@Slf4j
@RestController
public class OpenAiController {

    private final OpenAiStreamClient openAiStreamClient;
    private final OpenAiClient openAiClient;

    public OpenAiController(OpenAiStreamClient openAiStreamClient, OpenAiClient openAiClient) {
        this.openAiStreamClient = openAiStreamClient;
        this.openAiClient = openAiClient;
    }

    @RequestMapping(value = "/chat/completions", method = RequestMethod.POST)
    @CrossOrigin
    public Object chat(@RequestBody ChatCompletion req, @RequestHeader Map<String, String> headers) throws IOException {
        log.info("new chat request, stream: {}, msg: {}", req.isStream(), req.getMessages());

        if (req.isStream()) {
            SseEmitter sseEmitter = new SseEmitter(0L);
            try {
                sseEmitter.onCompletion(() -> log.info("chat finished"));
                sseEmitter.onTimeout(() -> log.info("chat timeout {}", sseEmitter.getTimeout()));
                sseEmitter.onError(
                        throwable -> {
                            try {
                                log.info("chat error: {}", throwable.toString());
                                sseEmitter.send(SseEmitter.event().name("chat error").
                                        data(throwable.getMessage()).reconnectTime(3000));
                            } catch (IOException e) {
                                log.error("stream chat error: {}", e.toString());
                            }
                        }
                );
                // 流式处理
                OpenAiChatListener listener = new OpenAiChatListener(sseEmitter);
                openAiStreamClient.streamChatCompletion(req, listener);
            } catch (Exception e) {
                throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
            }
            return sseEmitter;
        } else {
            try {
                // 非流式处理
                ChatCompletionResponse response = openAiClient.chatCompletion(req);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
            }
        }
    }


    @RequestMapping(value = "/completions", method = RequestMethod.POST)
    @CrossOrigin
    public Object completion(@RequestBody Completion req, @RequestHeader Map<String, String> headers) throws IOException {
        log.info("new completion request, stream: {}, msg: {}", req.isStream(), req.getPrompt());

        if (req.isStream()) {
            SseEmitter sseEmitter = new SseEmitter(0L);
            try {
                sseEmitter.onCompletion(() -> log.info("completion finished"));
                sseEmitter.onTimeout(() -> log.info("completion timeout {}", sseEmitter.getTimeout()));
                sseEmitter.onError(
                        throwable -> {
                            try {
                                log.info("completion error: {}", throwable.toString());
                                sseEmitter.send(SseEmitter.event().name("completion error").
                                        data(throwable.getMessage()).reconnectTime(3000));
                            } catch (IOException e) {
                                log.error("stream completion error: {}", e.toString());
                            }
                        }
                );
                // 流式处理
                OpenAiCompletionListener listener = new OpenAiCompletionListener(sseEmitter);
                openAiStreamClient.streamCompletions(req, listener);
            } catch (Exception e) {
                throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
            }
            return sseEmitter;
        } else {
            try {
                // 非流式处理
                CompletionResponse response = openAiClient.completions(req);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                throw new BaseException(CommonError.OPENAI_SERVER_ERROR);
            }
        }
    }
}