package org.example.api.config;

import okhttp3.OkHttpClient;
import org.example.api.downloader.DownloaderImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class DownloaderConfig {
    @Bean
    public DownloaderImpl downloader() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

        return DownloaderImpl.init(builder);
    }
}
