package com.kfyty.sdk.api.core.http;

import reactor.core.publisher.Mono;

/**
 * 描述: http 请求执行器
 *
 * @author kfyty725
 * @date 2021/11/11 17:51
 * @email kfyty725@hotmail.com
 */
public interface HttpRequestExecutor {
    /**
     * 将三方响应包装为自定义响应
     *
     * @param response 三方响应
     * @return 自定义响应
     */
    HttpResponse wrapResponse(Object response);

    /**
     * 返回 http 响应
     *
     * @return response
     */
    default HttpResponse exchange(HttpRequest<?> api) {
        return exchange(api, true);
    }

    /**
     * 返回 http 响应
     *
     * @return response
     */
    HttpResponse exchange(HttpRequest<?> api, boolean validStatusCode);

    /**
     * 执行一个 http 请求，并返回二进制 body
     *
     * @param api http 请求
     * @return body
     */
    default byte[] execute(HttpRequest<?> api) {
        try (HttpResponse response = this.exchange(api)) {
            return response.body();
        }
    }

    /**
     * @see this#exchange(HttpRequest)
     */
    default Mono<HttpResponse> exchangeAsync(HttpRequest<?> api) {
        return this.exchangeAsync(api, true);
    }

    /**
     * @see this#exchange(HttpRequest, boolean)
     */
    default Mono<HttpResponse> exchangeAsync(HttpRequest<?> api, boolean validStatusCode) {
        return Mono.fromCallable(() -> this.exchange(api, validStatusCode));
    }

    /**
     * @see this#execute(HttpRequest)
     */
    default Mono<byte[]> executeAsync(HttpRequest<?> api) {
        return this.exchangeAsync(api).map(HttpResponse::body);
    }
}
