package com.ericsson.mxe.jcat.driver.prometheus;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Objects;

import org.apache.commons.net.util.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.ericsson.mxe.jcat.json.prometheus.PrometheusMetric;
import com.ericsson.mxe.jcat.json.prometheus.PrometheusRangeMetric;

public class PrometheusDriver {

    final RestTemplate restTemplate;
    final String url;
    final String user;
    final String password;
    final HttpHeaders headers;

    public PrometheusDriver(final String url) {
        restTemplate = new RestTemplate();
        this.url = normalizeUrl(url);
        this.user = null;
        this.password = null;
        headers = createHeaders();
    }

    public PrometheusDriver(final String url, final String user, final String password) {
        restTemplate = new RestTemplate();
        this.url = normalizeUrl(url);
        this.user = user;
        this.password = password;
        headers = createHeaders();
    }

    public PrometheusMetric query(final String query) {
        final LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("query", query);

        return restTemplate.exchange(url + "/api/v1/query", HttpMethod.POST, new HttpEntity<>(form, headers), PrometheusMetric.class).getBody();
    }

    public PrometheusRangeMetric queryRange(final long start, final long end, final String step, final String query) {
        final LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("start", Long.toString(start));
        form.add("end", Long.toString(end));
        form.add("step", step);
        form.add("query", query);

        return restTemplate.exchange(url + "/api/v1/query_range", HttpMethod.POST, new HttpEntity<>(form, headers), PrometheusRangeMetric.class).getBody();
    }

    private String normalizeUrl(final String url) {
        try {
            return new URI(url).normalize().toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Wrong Prometheus URL", e);
        }
    }

    private HttpHeaders createHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        if (Objects.nonNull(user) && Objects.nonNull(password)) {
            final String auth = user + ":" + password;
            final Charset charset = Charset.forName("UTF-8");
            final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(charset));
            final String authHeader = "Basic " + new String(encodedAuth, charset);
            httpHeaders.set("Authorization", authHeader);
        }

        return httpHeaders;
    }

}
