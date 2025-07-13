// --- 修改后的 GsonFactory.java ---

package com.hjq.gson.factory;

import android.annotation.SuppressLint;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.ReflectionAccessFilter;
import com.google.gson.ToNumberStrategy;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.TypeAdapters;
import com.hjq.gson.factory.constructor.MainConstructor;
import com.hjq.gson.factory.data.BigDecimalTypeAdapter;
import com.hjq.gson.factory.data.BooleanTypeAdapter;
import com.hjq.gson.factory.data.DoubleTypeAdapter;
import com.hjq.gson.factory.data.FloatTypeAdapter;
import com.hjq.gson.factory.data.IntegerTypeAdapter;
import com.hjq.gson.factory.data.JSONArrayTypeAdapter;
import com.hjq.gson.factory.data.JSONObjectTypeAdapter;
import com.hjq.gson.factory.data.LongTypeAdapter;
import com.hjq.gson.factory.data.StringTypeAdapter;
import com.hjq.gson.factory.element.CollectionTypeAdapterFactory;
import com.hjq.gson.factory.element.MapTypeAdapterFactory;
import com.hjq.gson.factory.element.ReflectiveTypeAdapterFactory;
import com.hjq.gson.factory.other.AutoToNumberStrategy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("unused")
public final class GsonFactory {

    private static ParseExceptionCallback sParseExceptionCallback;
    private static volatile Gson sGson;

    private GsonFactory() {}

    /**
     * 获取单例的 Gson 对象
     */
    public static Gson getSingletonGson() {
        if (sGson == null) {
            synchronized (GsonFactory.class) {
                if (sGson == null) {
                    // 修改：使用新的 Builder 模式创建
                    sGson = new Builder().create();
                }
            }
        }
        return sGson;
    }

    /**
     * 设置单例的 Gson 对象
     */
    public static void setSingletonGson(Gson gson) {
        sGson = gson;
    }

    /**
     * 设置 Json 解析出错回调对象
     */
    public static void setParseExceptionCallback(ParseExceptionCallback callback) {
        GsonFactory.sParseExceptionCallback = callback;
    }

    /**
     * 获取 Json 解析出错回调对象（可能为空）
     */
    public static ParseExceptionCallback getParseExceptionCallback() {
        return sParseExceptionCallback;
    }

    /**
     * @deprecated 已过时，请使用 Builder 代替
     */
    @Deprecated
    public static void registerTypeAdapterFactory(TypeAdapterFactory factory) {
        // 这个方法现在变得危险，因为它影响全局状态。最好通过 Builder 配置。
        // 为了保持旧功能，可以创建一个静态列表，但在 Builder 中使用它。
    }

    /**
     * @deprecated 已过时，请使用 Builder 代替
     */
    @Deprecated
    public static void registerInstanceCreator(Type type, InstanceCreator<?> creator) {
        // 同上，最好通过 Builder 配置
    }

    /**
     * @deprecated 已过时，请使用 Builder 代替
     */
    @Deprecated
    public static void addReflectionAccessFilter(ReflectionAccessFilter filter) {
        // 同上
    }

    /**
     * @deprecated 已过时，请使用 Builder 代替
     */
    @Deprecated
    public static void setObjectToNumberStrategy(ToNumberStrategy objectToNumberStrategy) {
        // 同上
    }

    /**
     * 创建一个新的 Gson 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @deprecated 已过时，请使用 builder().create()
     */
    @SuppressLint("CheckResult")
    @Deprecated
    public static GsonBuilder newGsonBuilder() {
        // 这个方法应该被废弃，因为它无法传递命名策略
        return new Builder().createGsonBuilder();
    }

    // ===================================================================================
    // 新增：标准的 Builder 模式
    // ===================================================================================

    public static final class Builder {
        private final HashMap<Type, InstanceCreator<?>> instanceCreators = new HashMap<>();
        private final List<TypeAdapterFactory> typeAdapterFactories = new ArrayList<>();
        private final List<ReflectionAccessFilter> reflectionAccessFilters = new ArrayList<>();
        private FieldNamingStrategy fieldNamingStrategy;
        private ToNumberStrategy objectToNumberStrategy;

        public Builder() {
            this.fieldNamingStrategy = FieldNamingPolicy.IDENTITY;
            this.objectToNumberStrategy = new AutoToNumberStrategy();
        }

        public Builder setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
            this.fieldNamingStrategy = fieldNamingStrategy;
            return this;
        }

        public Builder setObjectToNumberStrategy(ToNumberStrategy objectToNumberStrategy) {
            this.objectToNumberStrategy = objectToNumberStrategy;
            return this;
        }

        public Builder registerTypeAdapterFactory(TypeAdapterFactory factory) {
            this.typeAdapterFactories.add(factory);
            return this;
        }

        public Builder registerInstanceCreator(Type type, InstanceCreator<?> creator) {
            this.instanceCreators.put(type, creator);
            return this;
        }

        public Builder addReflectionAccessFilter(ReflectionAccessFilter filter) {
            if (filter != null) {
                this.reflectionAccessFilters.add(0, filter);
            }
            return this;
        }

        public Gson create() {
            return createGsonBuilder().create();
        }

        @SuppressLint("CheckResult")
        public GsonBuilder createGsonBuilder() {
            GsonBuilder gsonBuilder = new GsonBuilder();
            MainConstructor mainConstructor = new MainConstructor(instanceCreators, true, reflectionAccessFilters);

            if (objectToNumberStrategy != null) {
                gsonBuilder.setObjectToNumberStrategy(objectToNumberStrategy);
            }

            gsonBuilder.registerTypeAdapterFactory(TypeAdapters.newFactory(String.class, new StringTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(boolean.class, Boolean.class, new BooleanTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(int.class, Integer.class, new IntegerTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(long.class, Long.class, new LongTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(float.class, Float.class, new FloatTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(double.class, Double.class, new DoubleTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(BigDecimal.class, new BigDecimalTypeAdapter()))
                    .registerTypeAdapterFactory(new CollectionTypeAdapterFactory(mainConstructor))
                    .registerTypeAdapterFactory(new ReflectiveTypeAdapterFactory(mainConstructor, fieldNamingStrategy, Excluder.DEFAULT))
                    .registerTypeAdapterFactory(new MapTypeAdapterFactory(mainConstructor, false))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(JSONObject.class, new JSONObjectTypeAdapter()))
                    .registerTypeAdapterFactory(TypeAdapters.newFactory(JSONArray.class, new JSONArrayTypeAdapter()));

            for (TypeAdapterFactory typeAdapterFactory : typeAdapterFactories) {
                gsonBuilder.registerTypeAdapterFactory(typeAdapterFactory);
            }
            return gsonBuilder;
        }
    }
}