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

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import qm.vp.kiev.qmhttplib.QMErrors;
import qm.vp.kiev.qmhttplib.abstraction.QMError;
import retrofit.converter.Converter;
//import retrofit.http.HTTP;
//import retrofit.http.Header;

/**
 * Adapts a Java interface to a REST API.
 * <p/>
 * API endpoints are defined as methods on an interface with annotations providing metadata about
 * the form in which the HTTP call should be made.
 * <p/>
 * The relative path for a given method is obtained from an annotation on the method describing
 * the request type. The built-in methods are {@link retrofit.http.GET GET},
 * {@link retrofit.http.PUT PUT}, {@link retrofit.http.POST POST}, {@link retrofit.http.POST PATCH},
 * {@link retrofit.http.HEAD HEAD}, and {@link retrofit.http.DELETE DELETE}. You can use a custom
 * HTTP method with {@link retrofit.http.HTTP @HTTP}.
 * <p/>
 * Method parameters can be used to replace parts of the URL by annotating them with
 * {@link retrofit.http.Path @Path}. Replacement sections are denoted by an identifier surrounded
 * by curly braces (e.g., "{foo}"). To add items to the query string of a URL use
 * {@link retrofit.http.Query @Query}.
 * <p/>
 * HTTP requests happen in one of two ways:
 * <ul>
 * <li>On the provided HTTP {@link java.util.concurrent.Executor} with callbacks marshaled to the callback
 * {@link java.util.concurrent.Executor}. The last method parameter should be of type {@link retrofit.Callback}. The HTTP
 * response will be converted to the callback's parameter type using the specified
 * {@link retrofit.converter.Converter Converter}. If the callback parameter type uses a wildcard,
 * the lower bound will be used as the conversion type.
 * <li>On the current thread returning the response or throwing a {@link RetrofitError}. The HTTP
 * response will be converted to the method's return type using the specified
 * {@link retrofit.converter.Converter Converter}.
 * </ul>
 * <p/>
 * The body of a request is denoted by the {@link retrofit.http.Body @Body} annotation. The object
 * will be converted to request representation by a call to
 * {@link retrofit.converter.Converter#toBody(Object) toBody} on the supplied
 * {@link retrofit.converter.Converter Converter} for this instance. The body can also be a
 * {@link TypedOutput} where it will be used directly.
 * <p/>
 * Alternative request body formats are supported by method annotations and corresponding parameter
 * annotations:
 * <ul>
 * <li>{@link retrofit.http.FormUrlEncoded @FormUrlEncoded} - Form-encoded data with key-value
 * pairs specified by the {@link retrofit.http.Field @Field} parameter annotation.
 * <li>{@link retrofit.http.Multipart @Multipart} - RFC 2387-compliant multi-part data with parts
 * specified by the {@link retrofit.http.Part @Part} parameter annotation.
 * </ul>
 * <p/>
 * Additional static headers can be added for an endpoint using the
 * {@link retrofit.http.Headers @Headers} method annotation. For per-request control over a header
 * annotate a parameter with {@link retrofit.http.Header @Header}.
 * <p/>
 * For example:
 * <pre>
 * public interface MyApi {
 *   &#64;POST("/category/{cat}") // Asynchronous execution.
 *   void categoryList(@Path("cat") String a, @Query("page") int b,
 *                     Callback&lt;List&lt;Item&gt;&gt; cb);
 *   &#64;POST("/category/{cat}") // Synchronous execution.
 *   List&lt;Item&gt; categoryList(@Path("cat") String a, @Query("page") int b);
 * }
 * </pre>
 * <p/>
 * Calling {@link #create(Class)} with {@code MyApi.class} will validate and create a new
 * implementation of the API.
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 */
public class RestAdapter {
    private final Map<Class<?>, Map<Method, MethodInfo>> serviceMethodInfoCache =
            new LinkedHashMap<Class<?>, Map<Method, MethodInfo>>();

    final Endpoint endpoint;
    final Executor callbackExecutor;

    private final OkHttpClient client;

    private RestAdapter(Endpoint endpoint, OkHttpClient client, Executor callbackExecutor) {
        this.endpoint = endpoint;
        this.client = client;
        this.callbackExecutor = callbackExecutor;
//        this.requestInterceptor = requestInterceptor;
//        this.converter = converter;
//        this.errorHandler = errorHandler;
    }

    /**
     * Create an implementation of the API defined by the specified {@code service} interface.
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> service) {
        Utils.validateServiceClass(service);
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new RestHandler(getMethodInfoCache(service)));
    }

    Map<Method, MethodInfo> getMethodInfoCache(Class<?> service) {
        synchronized (serviceMethodInfoCache) {
            Map<Method, MethodInfo> methodInfoCache = serviceMethodInfoCache.get(service);
            if (methodInfoCache == null) {
                methodInfoCache = new LinkedHashMap<Method, MethodInfo>();
                serviceMethodInfoCache.put(service, methodInfoCache);
            }
            return methodInfoCache;
        }
    }

    static MethodInfo getMethodInfo(Map<Method, MethodInfo> cache, Method method) {
        synchronized (cache) {
            MethodInfo methodInfo = cache.get(method);
            if (methodInfo == null) {
                methodInfo = new MethodInfo(method);
                cache.put(method, methodInfo);
            }
            return methodInfo;
        }
    }

    private class RestHandler implements InvocationHandler {
        private final Map<Method, MethodInfo> methodDetailsCache;

        RestHandler(Map<Method, MethodInfo> methodDetailsCache) {
            this.methodDetailsCache = methodDetailsCache;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object invoke(Object proxy, Method method, final Object[] args)
                throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            MethodInfo methodInfo = getMethodInfo(methodDetailsCache, method);
            Request request = createRequest(methodInfo, args);

            invokeAsync(methodInfo, request, (Callback) args[args.length - 1]);
            return null;

        }

        private void invokeAsync(final MethodInfo methodInfo, final Request request,
                                 final Callback callback) {
            Call call = client.newCall(request);
            call.enqueue(new com.squareup.okhttp.Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    callback.failure(QMErrors.UNKNOWN);
//                    callFailure(callback, RetrofitError.networkFailure(request.urlString(), e));
                }

                @Override
                public void onResponse(Response response) {
                    try {
//                        Object result = createResult(methodInfo, response);
                        callResponse(callback, IOUtils.toString(response.body().byteStream()), response);
                    } catch (RetrofitError error) {
                        callFailure(callback, error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void callResponse(final Callback callback, final Object result,
                                  final Response response) {
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.success(result, response);
                }
            });
        }

        private Request createRequest(MethodInfo methodInfo, Object[] args) {
            String serverUrl = endpoint.url();
            RequestBuilder requestBuilder = new RequestBuilder(serverUrl, methodInfo);
            requestBuilder.setArguments(args);

//            requestInterceptor.intercept(requestBuilder);

            return requestBuilder.build();
        }
    }

    /**
     * Build a new {@link retrofit.RestAdapter}.
     * <p/>
     * Calling {@link #setEndpoint} is required before calling {@link #build()}. All other methods
     * are optional.
     */
    public static class Builder {
        private Endpoint endpoint;
        private OkHttpClient client;
        private Executor callbackExecutor;
//        private RequestInterceptor requestInterceptor;
        //        private Converter converter;
//        private ErrorHandler errorHandler;

        /**
         * API endpoint URL.
         */
//        public Builder setEndpoint(String url) {
//            return setEndpoint(Endpoint.createFixed(url));
//        }

        /**
         * API endpoint.
         */
        public Builder setEndpoint(Endpoint endpoint) {
            if (endpoint == null) {
                throw new NullPointerException("Endpoint may not be null.");
            }
            this.endpoint = endpoint;
            return this;
        }

        /**
         * The HTTP client used for requests.
         */
        public Builder setClient(OkHttpClient client) {
            if (client == null) {
                throw new NullPointerException("Client may not be null.");
            }
            this.client = client;
            return this;
        }

        /**
         * Executor on which any {@link retrofit.Callback} methods will be invoked. If this argument is
         * {@code null} then callback methods will be run on the same thread as the HTTP client.
         */
        public Builder setCallbackExecutor(Executor callbackExecutor) {
            if (callbackExecutor == null) {
                callbackExecutor = new Utils.SynchronousExecutor();
            }
            this.callbackExecutor = callbackExecutor;
            return this;
        }

        /**
         * A request interceptor for adding data to every request.
         */
//        public Builder setRequestInterceptor(RequestInterceptor requestInterceptor) {
//            if (requestInterceptor == null) {
//                throw new NullPointerException("Request interceptor may not be null.");
//            }
//            this.requestInterceptor = requestInterceptor;
//            return this;
//        }

        /**
         * The converter used for serialization and deserialization of objects.
         */
//        public Builder setConverter(Converter converter) {
//            if (converter == null) {
//                throw new NullPointerException("Converter may not be null.");
//            }
//            this.converter = converter;
//            return this;
//        }

        /**
         * The error handler allows you to customize the type of exception thrown for errors on
         * synchronous requests.
         */
//        public Builder setErrorHandler(ErrorHandler errorHandler) {
//            if (errorHandler == null) {
//                throw new NullPointerException("Error handler may not be null.");
//            }
//            this.errorHandler = errorHandler;
//            return this;
//        }

        /**
         * Create the {@link retrofit.RestAdapter} instances.
         */
        public RestAdapter build() {
//            if (endpoint == null) {
//                throw new IllegalArgumentException("Endpoint may not be null.");
//            }
//            ensureSaneDefaults();
            return new RestAdapter(endpoint, client, callbackExecutor);
        }

//        private void ensureSaneDefaults() {
////            if (converter == null) {
////                converter = Platform.get().defaultConverter();
////            }
//            if (client == null) {
//                client = Platform.get().defaultClient();
//            }
//            if (callbackExecutor == null) {
//                callbackExecutor = Platform.get().defaultCallbackExecutor();
//            }
//            if (errorHandler == null) {
//                errorHandler = ErrorHandler.DEFAULT;
//            }
//            if (requestInterceptor == null) {
//                requestInterceptor = RequestInterceptor.NONE;
//            }
//        }
    }
}
