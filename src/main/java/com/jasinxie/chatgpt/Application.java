package com.jasinxie.chatgpt;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

	@Value("${openai.apiKey}")
	private String apiKey;
	@Value("${openai.apiHost}")
	private String apiHost;


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public OpenAiStreamClient openAiStreamClient() {
		return OpenAiStreamClient.builder().apiHost(apiHost).apiKey(Collections.singletonList(apiKey)).build();
	}

	@Bean
	public OpenAiClient openAiClient() {
		return OpenAiClient.builder().apiHost(apiHost).apiKey(Collections.singletonList(apiKey)).build();
	}

}