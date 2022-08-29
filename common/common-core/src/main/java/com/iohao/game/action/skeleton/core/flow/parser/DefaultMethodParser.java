/*
 * # iohao.com . 渔民小镇
 * Copyright (C) 2021 - 2022 double joker （262610965@qq.com） . All Rights Reserved.
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
import com.iohao.game.common.kit.ProtoKit;

/**
 * 默认解析器
 *
 * @author 渔民小镇
 * @date 2022-06-26
 */

final class DefaultMethodParser implements MethodParser {

    @Override
    public Class<?> getActualClazz(ActionCommand.MethodParamResultInfo methodParamResultInfo) {
        return methodParamResultInfo.getActualTypeArgumentClazz();
    }

    @Override
    public Object parseParam(byte[] data, ActionCommand.ParamInfo paramInfo) {
        Class<?> actualTypeArgumentClazz = paramInfo.getActualTypeArgumentClazz();
        return ProtoKit.parseProtoByte(data, actualTypeArgumentClazz);
    }

    @Override
    public Object parseResult(ActionCommand.ActionMethodReturnInfo actionMethodReturnInfo, Object methodResult) {
        return methodResult;
    }

    @Override
    public boolean isCustomMethodParser() {
        return false;
    }

    private DefaultMethodParser() {

    }

    public static DefaultMethodParser me() {
        return Holder.ME;
    }


    /** 通过 JVM 的类加载机制, 保证只加载一次 (singleton) */
    private static class Holder {
        static final DefaultMethodParser ME = new DefaultMethodParser();
    }
}
