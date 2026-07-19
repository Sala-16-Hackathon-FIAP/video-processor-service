package br.com.fiapx.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VideoProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoProcessorApplication.class, args);
    }
}
