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
package com.iohao.game.action.skeleton.core.flow.codec;

import com.iohao.game.action.skeleton.core.DataCodecKit;

/**
 * 业务数据的编解码器
 * <pre>
 *     see {@link DataCodecKit}
 * </pre>
 *
 * @author 渔民小镇
 * @date 2022-05-18
 */
public interface DataCodec {
    /**
     * 将数据对象编码成字节数组
     *
     * @param data 数据对象
     * @return bytes
     */
    byte[] encode(Object data);

    /**
     * 将字节数组解码成对象
     *
     * @param data      数据对象的字节
     * @param dataClass 数据对象 class
     * @param <T>       t
     * @return 业务参数
     */
    <T> T decode(byte[] data, Class<?> dataClass);

    /**
     * 编解码名
     *
     * @return 编解码名
     */
    default String codecName() {
        return this.getClass().getSimpleName();
    }
}
