/*
 * Copyright (C) 2013 Square, Inc.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qm.vp.kiev.qmhttplib.retrofit.http.GET;
import qm.vp.kiev.qmhttplib.retrofit.http.POST;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Part;


/**
 * Request metadata about a service interface declaration.
 */
final class MethodInfo {

    // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
    private static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    private static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
    private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

    enum RequestType {
        /**
         * No content-specific logic required.
         */
        SIMPLE,
        /**
         * Multi-part request body.
         */
        MULTIPART,
        /**
         * Form URL-encoded request body.
         */
        FORM_URL_ENCODED
    }

    final Method method;

    String requestMethod;
    boolean requestHasBody;
    String requestUrl;
    Set<String> requestUrlParamNames;
    String requestQuery;

    Annotation[] requestParamAnnotations;

    MethodInfo(Method method) {
        this.method = method;

        parseMethodAnnotations();
        parseParameters();
    }

    private RuntimeException methodError(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        return new IllegalArgumentException(
                method.getDeclaringClass().getSimpleName() + "." + method.getName() + ": " + message);
    }

    private RuntimeException parameterError(int index, String message, Object... args) {
        return methodError(message + " (parameter #" + (index + 1) + ")", args);
    }

    /**
     * Loads {@link #requestMethod} and {@link #requestType}.
     */
    private void parseMethodAnnotations() {
        for (Annotation methodAnnotation : method.getAnnotations()) {
            Class<? extends Annotation> annotationType = methodAnnotation.annotationType();

            if (annotationType == GET.class) {
                parseHttpMethodAndPath("GET", ((GET) methodAnnotation).value(), false);
            } else if (annotationType == POST.class) {
                parseHttpMethodAndPath("POST", ((POST) methodAnnotation).value(), true);
            }
        }

        if (requestMethod == null) {
            throw methodError("HTTP method annotation is required (e.g., @GET, @POST, etc.).");
        }
    }

    /**
     * Loads {@link #requestUrl}, {@link #requestUrlParamNames}, and {@link #requestQuery}.
     */
    private void parseHttpMethodAndPath(String method, String path, boolean hasBody) {
        if (requestMethod != null) {
            throw methodError("Only one HTTP method is allowed. Found: %s and %s.", requestMethod,
                    method);
        }
        if (path == null || path.length() == 0 || path.charAt(0) != '/') {
            throw methodError("URL path \"%s\" must start with '/'.", path);
        }

        // Get the relative URL path and existing query string, if present.
        String url = path;
        String query = null;
        int question = path.indexOf('?');
        if (question != -1 && question < path.length() - 1) {
            url = path.substring(0, question);
            query = path.substring(question + 1);

            // Ensure the query string does not have any named parameters.
            Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(query);
            if (queryParamMatcher.find()) {
                throw methodError("URL query string \"%s\" must not have replace block. For dynamic query"
                        + " parameters use @Query.", query);
            }
        }

        Set<String> urlParams = parsePathParameters(path);

        requestMethod = method;
        requestHasBody = hasBody;
        requestUrl = url;
        requestUrlParamNames = urlParams;
        requestQuery = query;
    }

    private static Type getParameterUpperBound(ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        for (int i = 0; i < types.length; i++) {
            Type paramType = types[i];
            if (paramType instanceof WildcardType) {
                types[i] = ((WildcardType) paramType).getUpperBounds()[0];
            }
        }
        return types[0];
    }

    /**
     * Loads {@link #requestParamAnnotations}. Must be called after {@link #parseMethodAnnotations()}.
     */
    private void parseParameters() {
        Type[] methodParameterTypes = method.getGenericParameterTypes();

        Annotation[][] methodParameterAnnotationArrays = method.getParameterAnnotations();
        int count = methodParameterAnnotationArrays.length;

//        if (executionType == ExecutionType.ASYNC) {
        count -= 1; // Callback is last argument when not a synchronous method.
//        }

        Annotation[] requestParamAnnotations = new Annotation[count];

        for (int i = 0; i < count; i++) {
            Type methodParameterType = methodParameterTypes[i];
            Annotation[] methodParameterAnnotations = methodParameterAnnotationArrays[i];
            if (methodParameterAnnotations != null) {
                for (Annotation methodParameterAnnotation : methodParameterAnnotations) {
                    Class<? extends Annotation> methodAnnotationType =
                            methodParameterAnnotation.annotationType();

                    if (requestParamAnnotations[i] != null) {
                        throw parameterError(i,
                                "Multiple Retrofit annotations found, only one allowed: @%s, @%s.",
                                requestParamAnnotations[i].annotationType().getSimpleName(),
                                methodAnnotationType.getSimpleName());
                    }
                    requestParamAnnotations[i] = methodParameterAnnotation;
                }
            }

            if (requestParamAnnotations[i] == null) {
                throw parameterError(i, "No Retrofit annotation found.");
            }
        }

        this.requestParamAnnotations = requestParamAnnotations;
    }

    private void validatePathName(int index, String name) {
        if (!PARAM_NAME_REGEX.matcher(name).matches()) {
            throw parameterError(index, "@Path parameter name must match %s. Found: %s",
                    PARAM_URL_REGEX.pattern(), name);
        }
        // Verify URL replacement name is actually present in the URL path.
        if (!requestUrlParamNames.contains(name)) {
            throw parameterError(index, "URL \"%s\" does not contain \"{%s}\".", requestUrl, name);
        }
    }

    /**
     * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
     * in the URI, it will only show up once in the set.
     */
    static Set<String> parsePathParameters(String path) {
        Matcher m = PARAM_URL_REGEX.matcher(path);
        Set<String> patterns = new LinkedHashSet<String>();
        while (m.find()) {
            patterns.add(m.group(1));
        }
        return patterns;
    }
}
