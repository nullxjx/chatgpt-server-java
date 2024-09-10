# chatgpt-server-java

## 介绍
使用 springboot 实现的 chatgpt-server，适配了openai 的输入输出格式，支持流式调用和非流式调用。

## 使用指南
可以使用 [vllm](https://github.com/vllm-project/vllm) 部署好你的模型，然后在 [application.properties](src/main/resources/application.properties) 的 openai.apiHost 字段设置你的模型调用地址。

启动好服务之后，使用下面的命令测试
```bash
curl localhost:8080/v1/completions \
  -H "Content-Type: application/json" \
  -d '{
        "model": "codewise-7b",
        "prompt": "如何使用nginx进行负载均衡?",
        "max_tokens": 256,
        "temperature": 0.2,
        "stream": true
    }'

curl localhost:8080/v1/chat/completions \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "model": "codewise-7b",
    "messages": [
        {"role": "system", "content": "You are a helpful assistant."},
        {"role": "user", "content": "如何使用nginx进行负载均衡？"}
    ],
    "max_tokens": 256,
    "temperature": 1,
    "stream": true,
    "skip_special_tokens": false
  }'
```

## 参考文档
https://chatgpt-java.unfbx.com/docs/quick_start