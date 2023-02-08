/*
 * # iohao.com . 渔民小镇
 * Copyright (C) 2021 - 2023 double joker （262610965@qq.com） . All Rights Reserved.
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
package com.iohao.game.action.skeleton.core.flow.parser;

import com.iohao.game.action.skeleton.core.ActionCommand;
import com.iohao.game.action.skeleton.protocol.wrapper.*;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 关于业务框架中，action 参数相关的包装类
 *
 * @author 渔民小镇
 * @date 2022-06-26
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class MethodParsers {
    /**
     * 方法解析器 map
     * <pre>
     *     key : 一般用基础类型
     *     value : 基础类型对应的解析器
     *
     *     如 int 与对应的包装类 Integer 在这里规划为基础类型
     * </pre>
     */
    final Map<Class<?>, MethodParser> methodParserMap = new HashMap<>();
    final Map<Class<?>, Supplier<?>> paramSupplierMap = new HashMap<>();
    /** action 业务方法参数的默认解析器 */
    @Setter
    MethodParser methodParser = DefaultMethodParser.me();

    public void mappingParamSupplier(Class<?> paramClass, Supplier<?> supplier) {
        this.paramSupplierMap.put(paramClass, supplier);
    }

    public void mapping(Class<?> paramClass, MethodParser methodParamParser) {
        this.methodParserMap.put(paramClass, methodParamParser);
    }

    public MethodParser getMethodParser(ActionCommand.ActionMethodReturnInfo actionMethodReturnInfo) {
        Class<?> methodResultClass = actionMethodReturnInfo.getActualTypeArgumentClazz();
        return getMethodParser(methodResultClass);
    }

    public MethodParser getMethodParser(ActionCommand.ParamInfo paramInfo) {
        Class<?> actualTypeArgumentClazz = paramInfo.getActualTypeArgumentClazz();
        return getMethodParser(actualTypeArgumentClazz);
    }

    public void clear() {
        this.methodParserMap.clear();
    }

    public boolean containsKey(Class<?> clazz) {
        return this.methodParserMap.containsKey(clazz);
    }

    public Set<Class<?>> keySet() {
        return this.methodParserMap.keySet();
    }

    public MethodParser getMethodParser(Class<?> paramClazz) {
        return this.methodParserMap.getOrDefault(paramClazz, this.methodParser);
    }

    Object newObject(Class<?> paramClass) {
        if (this.paramSupplierMap.containsKey(paramClass)) {
            return this.paramSupplierMap.get(paramClass).get();
        }

        return null;
    }

    private void mapping(Class<?> paramClass, MethodParser methodParamParser, Supplier<?> supplier) {
        mapping(paramClass, methodParamParser);

        /*
         * 使用原生 pb 如果值为 0，在 jprotobuf 中会出现 nul 的情况，为了避免这个问题
         * 如果业务参数为 null，当解析到对应的类型时，则使用 Supplier 来创建对象
         *
         * 具体使用可参考 DefaultMethodParser
         */
        mappingParamSupplier(paramClass, supplier);
    }

    private MethodParsers() {
        // 表示在 action 参数中，遇见 int 类型的参数，用 IntMethodParamParser 来解析
        this.mapping(int.class, IntMethodParser.me());
        this.mapping(Integer.class, IntMethodParser.me());
        // 表示在 action 参数中，遇见 long 类型的参数，用 LongMethodParamParser 来解析
        this.mapping(long.class, LongMethodParser.me());
        this.mapping(Long.class, LongMethodParser.me());
        // 表示在 action 参数中，遇见 string 类型的参数，用 StringMethodParser 来解析
        this.mapping(String.class, StringMethodParser.me());
        // 表示在 action 参数中，遇见 boolean 类型的参数，用 BooleanMethodParamParser 来解析
        this.mapping(boolean.class, BooleanMethodParser.me());
        this.mapping(Boolean.class, BooleanMethodParser.me());

        /*
         * 这里注册是为了顺便使用 containsKey 方法，因为生成文档的时候要用到短名字
         * 当然也可以使用 instanceof 来做这些，但似乎没有这种方式优雅
         */
        this.mapping(IntPb.class, DefaultMethodParser.me(), IntPb::new);
        this.mapping(IntListPb.class, DefaultMethodParser.me(), IntListPb::new);

        this.mapping(LongPb.class, DefaultMethodParser.me(), LongPb::new);
        this.mapping(LongListPb.class, DefaultMethodParser.me(), LongListPb::new);

        this.mapping(StringPb.class, DefaultMethodParser.me(), StringPb::new);
        this.mapping(StringListPb.class, DefaultMethodParser.me(), StringListPb::new);

        this.mapping(BooleanPb.class, DefaultMethodParser.me(), BooleanPb::new);
        this.mapping(BooleanListPb.class, DefaultMethodParser.me(), BooleanListPb::new);
    }

    public static MethodParsers me() {
        return Holder.ME;
    }

    /** 通过 JVM 的类加载机制, 保证只加载一次 (singleton) */
    private static class Holder {
        static final MethodParsers ME = new MethodParsers();
    }
}
