/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package qm.vp.kiev.qmhttplib.retrofit;

import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;

import okio.BufferedSink;
import qm.vp.kiev.qmhttplib.retrofit.http.Part;
import qm.vp.kiev.qmhttplib.retrofit.http.Path;


final class RequestBuilder {
    private static final Headers NO_HEADERS = Headers.of();

    private static final String TAG = RequestBuilder.class.getName();


    //    private final Converter converter;
    private final Annotation[] paramAnnotations;
    //    private final String requestMethod;

    private final boolean async = true;

    private final String apiUrl;

    private MultipartBuilder multipartBuilder = new MultipartBuilder();

    private String relativeUrl;

    RequestBuilder(String apiUrl, MethodInfo methodInfo) {
        this.apiUrl = apiUrl;
        paramAnnotations = methodInfo.requestParamAnnotations;
        relativeUrl = methodInfo.requestUrl;
    }


    private void addPathParam(String name, String value, boolean urlEncodeValue) {
        if (name == null) {
            throw new IllegalArgumentException("Path replacement name must not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException(
                    "Path replacement \"" + name + "\" value must not be null.");
        }
        try {
            if (urlEncodeValue) {
                String encodedValue = URLEncoder.encode(String.valueOf(value), "UTF-8");
                // URLEncoder encodes for use as a query parameter. Path encoding uses %20 to
                // encode spaces rather than +. Query encoding difference specified in HTML spec.
                // Any remaining plus signs represent spaces as already URLEncoded.
                encodedValue = encodedValue.replace("+", "%20");
                relativeUrl = relativeUrl.replace("{" + name + "}", encodedValue);
            } else {
                relativeUrl = relativeUrl.replace("{" + name + "}", String.valueOf(value));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(
                    "Unable to convert path parameter \"" + name + "\" value to UTF-8:" + value, e);
        }
    }

    void setArguments(Object[] args) {
        if (args == null) {
            return;
        }
        int count = args.length;
        if (async) {
            count -= 1;
        }
        for (int i = 0; i < count; i++) {
            Object value = args[i];

            Annotation annotation = paramAnnotations[i];
            Class<? extends Annotation> annotationType = annotation.annotationType();

            if (annotationType == Part.class) {

                final String name = ((Part) annotation).value();

                multipartBuilder.addFormDataPart(name, String.valueOf(value));

            } else if (annotationType == Path.class) {
                Path path = (Path) annotation;
                String name = path.value();
                if (value == null) {
                    throw new IllegalArgumentException(
                            "Path parameter \"" + name + "\" value must not be null.");
                }
                addPathParam(name, value.toString(), path.encode());
            }
        }
    }

    Request build() {

        String apiUrl = this.apiUrl;
        StringBuilder url = new StringBuilder(apiUrl);
        if (apiUrl.endsWith("/")) {
            // We require relative paths to start with '/'. Prevent a double-slash.
            url.deleteCharAt(url.length() - 1);
        }

        url.append(relativeUrl);

        Log.e(TAG, String.format("build with url: %s", url.toString()));

        return new Request.Builder()
                .url(url.toString())
                .post(multipartBuilder.build())
                .build();
    }
}
