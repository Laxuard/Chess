package com.ft_transcendence.common.tracing;

import com.ft_transcendence.common.util.TraceContext;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.restclient.RestTemplateCustomizer;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

@Configuration
public class TracingConfig {

    @Bean
    public ClientHttpRequestInterceptor traceIdOutboundInterceptor() {
        return new ClientHttpRequestInterceptor() {
            @Override
            @NonNull
            public ClientHttpResponse intercept(@NonNull HttpRequest request,
                                                @NonNull byte[] body,
                                                @NonNull ClientHttpRequestExecution execution) throws IOException {
                String traceId = MDC.get(TraceContext.TRACE_KEY);
                if (traceId != null && !traceId.isBlank()) {
                    request.getHeaders().set(TraceContext.TRACE_HEADER, traceId);
                }
                return execution.execute(request, body);
            }
        };
    }

    @Bean
    @ConditionalOnClass(RestTemplateCustomizer.class)
    public RestTemplateCustomizer traceRestTemplateCustomizer(
            @org.springframework.beans.factory.annotation.Qualifier("traceIdOutboundInterceptor") ClientHttpRequestInterceptor interceptor) {
        return restTemplate -> restTemplate.getInterceptors().add(interceptor);
    }

    @Bean
    @ConditionalOnClass(RestClientCustomizer.class)
    public RestClientCustomizer traceRestClientCustomizer(
            @org.springframework.beans.factory.annotation.Qualifier("traceIdOutboundInterceptor") ClientHttpRequestInterceptor interceptor) {
        return restClientBuilder -> restClientBuilder.requestInterceptor(interceptor);
    }
}
