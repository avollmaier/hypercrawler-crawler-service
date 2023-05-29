package at.hypercrawler.crawlerservice.crawler.domain.service.crawl;

import at.hypercrawler.crawlerservice.manager.ConnectionHeader;
import at.hypercrawler.crawlerservice.manager.ConnectionProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.util.List;

@Slf4j
@Component
public class RequestOptionHttpClient {
    private final HttpClient client;

    public RequestOptionHttpClient() {

        client = HttpClient.create();

    }

    public RequestOptionHttpClient setProxy(ConnectionProxy proxy) {
        if (proxy != null) {
            log.info("Using proxy: " + proxy.host() + ":" + proxy.port());
            client.proxy(p -> p.type(ProxyProvider.Proxy.HTTP)
                    .host(proxy.host())
                    .port(proxy.port()));
        }
        return this;
    }

    public RequestOptionHttpClient setHeaders(List<ConnectionHeader> headers) {
        if (headers != null) {
            log.info("Using headers: " + headers);
            headers.forEach(header -> client.headers(httpHeaders -> httpHeaders.add(header.name(), header.value())));
        }
        return this;
    }

    public HttpClient build() {
        return client;
    }

}
