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
package com.iohao.game.bolt.broker.client.processor;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.iohao.game.action.skeleton.protocol.processor.ExtRequestMessage;
import com.iohao.game.action.skeleton.protocol.processor.ExtResponseMessage;
import com.iohao.game.bolt.broker.core.aware.BrokerClientAware;
import com.iohao.game.bolt.broker.core.client.BrokerClient;
import com.iohao.game.bolt.broker.core.common.AbstractAsyncUserProcessor;
import com.iohao.game.bolt.broker.core.ext.ExtRegion;
import com.iohao.game.bolt.broker.core.ext.ExtRegionContext;
import com.iohao.game.bolt.broker.core.ext.ExtRegions;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 接收扩展逻辑服的消息
 * <pre>
 *     这个消息处理器用于接收所有扩展逻辑服的请求
 *     所有的逻辑服会接收此消息
 * </pre>
 *
 * @author 渔民小镇
 * @date 2022-05-30
 */
@Slf4j
public class ExtRequestMessageClientProcessor extends AbstractAsyncUserProcessor<ExtRequestMessage>
        implements BrokerClientAware {

    @Setter
    BrokerClient brokerClient;

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, ExtRequestMessage request) {

        // 从哪里来的
        int sourceClientId = request.getSourceClientId();

        ExtRegion extRegion = ExtRegions.me().getExtRegion(sourceClientId);

        if (Objects.isNull(extRegion)) {
            log.warn("ExtRegion 不存在 {}", request);
            return;
        }

        ExtRegionContext extRegionContext = new ExtRegionContext()
                .setBrokerClient(this.brokerClient)
                .setRequest(request);

        ExtResponseMessage responseMessage = extRegion.request(extRegionContext);

        if (Objects.isNull(responseMessage)) {
            return;
        }

        // 响应数据给来源端
        responseMessage.setSourceClientId(sourceClientId);

        try {
            brokerClient.oneway(responseMessage);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 指定感兴趣的请求数据类型，该 UserProcessor 只对感兴趣的请求类型的数据进行处理；
     * 假设 除了需要处理 MyRequest 类型的数据，还要处理 java.lang.String 类型，有两种方式：
     * 1、再提供一个 UserProcessor 实现类，其 interest() 返回 java.lang.String.class.getName()
     * 2、使用 MultiInterestUserProcessor 实现类，可以为一个 UserProcessor 指定 List<String> multiInterest()
     *
     * @return 自定义处理器
     */
    @Override
    public String interest() {
        return ExtRequestMessage.class.getName();
    }
}
