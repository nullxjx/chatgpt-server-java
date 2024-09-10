package com.jasinxie.chatgpt.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.completions.CompletionResponse;
import java.io.IOException;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Slf4j
public class OpenAiCompletionListener extends EventSourceListener {

    private SseEmitter sseEmitter;

    public OpenAiCompletionListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        if (data.equals("[DONE]")) {
            log.info("OpenAI返回数据结束了");
            sseEmitter.send(SseEmitter.event().data("[DONE]"));
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        CompletionResponse resp = mapper.readValue(data, CompletionResponse.class);
        sseEmitter.send(SseEmitter.event().data(resp));
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
        // 关闭连接
        eventSource.cancel();
        // 关闭 SseEmitter
        sseEmitter.complete();
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if (Objects.isNull(response)) {
            return;
        }

        try {
            // 检查是否是客户端中止异常
            if (t instanceof ClientAbortException ||
                    (t instanceof IOException && "Broken pipe".equals(t.getMessage()))) {
                log.warn("客户端中止了连接：{}", t.getMessage());
                return;
            }

            ResponseBody body = response.body();
            if (Objects.nonNull(body)) {
                String bodyString = body.string();
                log.error("OpenAI sse连接异常 body: {}", bodyString);
                sseEmitter.send(SseEmitter.event().id("chatcmpl-" + System.currentTimeMillis()).
                        data("Error: " + bodyString).name("Error"));
            } else if (t != null) {
                log.error("OpenAI sse连接异常 body: {}, 异常：{}", response, t.toString());
                sseEmitter.send(SseEmitter.event().id("chatcmpl-" + System.currentTimeMillis()).
                        data("Error: " + t.getMessage()).name("Error"));
            }
        } finally {
            // 确保资源被释放
            eventSource.cancel();
            // 关闭 SseEmitter
            sseEmitter.complete();
        }
    }
}
