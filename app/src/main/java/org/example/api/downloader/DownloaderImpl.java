package org.example.api.downloader;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import spark.utils.StringUtils;

import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class DownloaderImpl extends Downloader {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0";

    private static DownloaderImpl instance;
    private String mCookies;
    private final OkHttpClient client;

    private DownloaderImpl(OkHttpClient.Builder builder) {
        this.client = builder
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static DownloaderImpl init(@Nullable OkHttpClient.Builder builder) {
        return instance = new DownloaderImpl(builder != null ? builder : new OkHttpClient.Builder());
    }

    public static DownloaderImpl getInstance() {
        return instance;
    }

    public String getCookies() {
        return mCookies;
    }

    public void setCookies(String cookies) {
        mCookies = cookies;
    }

    public long getContentLength(String url) throws IOException {
        try {
            final Response response = head(url);
            String contentLength = response.getHeader("Content-Length");
            if (contentLength == null) {
                throw new IOException("Content-Length header is missing");
            }
            return Long.parseLong(contentLength);
//            return Long.parseLong(Objects.requireNonNull(response.getHeader("Content-Length")));
        } catch (NumberFormatException e) {
            throw new IOException("Invalid content length", e);
        } catch (ReCaptchaException e) {
            throw new IOException(e);
        }
    }

    public InputStream stream(String siteUrl) throws IOException {
        try {
            final okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                    .method("GET", null).url(siteUrl)
                    .addHeader("User-Agent", USER_AGENT);

            if (!StringUtils.isEmpty(mCookies)) {
                requestBuilder.addHeader("Cookie", mCookies);
            }

            final okhttp3.Request request = requestBuilder.build();
            final okhttp3.Response response = client.newCall(request).execute();
            final ResponseBody body = response.body();

            if (response.code() == 429) {
                throw new ReCaptchaException("reCaptcha Challenge requested", siteUrl);
            }

            if (body == null) {
                response.close();
                return null;
            }

            return body.byteStream();
        } catch (ReCaptchaException e) {
            throw new IOException(e.getMessage(), e.getCause());
        }
    }
//  @Override
    public Response execute(@NonNull Request request) throws IOException, ReCaptchaException {
        try {
            final String httpMethod = request.httpMethod();
            final String url = request.url();
            final Map<String, List<String>> headers = request.headers();
            final byte[] dataToSend = request.dataToSend();

            RequestBody requestBody = null;
            if (dataToSend != null) {
                requestBody = RequestBody.create(null, dataToSend);
            }

            final okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder()
                    .method(httpMethod, requestBody).url(url)
                    .addHeader("User-Agent", USER_AGENT);

            if (!StringUtils.isEmpty(mCookies)) {
                requestBuilder.addHeader("Cookie", mCookies);
            }

            for (Map.Entry<String, List<String>> pair : headers.entrySet()) {
                final String headerName = pair.getKey();
                final List<String> headerValueList = pair.getValue();

                if (headerValueList.size() > 1) {
                    requestBuilder.removeHeader(headerName);
                    for (String headerValue : headerValueList) {
                        requestBuilder.addHeader(headerName, headerValue);
                    }
                } else if (headerValueList.size() == 1) {
                    requestBuilder.header(headerName, headerValueList.get(0));
                }

            }

            final okhttp3.Response response = client.newCall(requestBuilder.build()).execute();

            if (response.code() == 429) {
                response.close();

                throw new ReCaptchaException("reCaptcha Challenge requested", url);
            }

            final ResponseBody body = response.body();
            String responseBodyToReturn = null;

            if (body != null) {
                responseBodyToReturn = body.string();
            }

            return new Response(response.code(), response.message(), response.headers().toMultimap(), responseBodyToReturn, url);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid URL: " + request.url(), e);
        }
    }
}
