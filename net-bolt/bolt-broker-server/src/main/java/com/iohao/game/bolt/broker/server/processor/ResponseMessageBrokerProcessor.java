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
package com.iohao.game.bolt.broker.server.processor;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.exception.RemotingException;
import com.iohao.game.action.skeleton.protocol.HeadMetadata;
import com.iohao.game.action.skeleton.protocol.ResponseMessage;
import com.iohao.game.bolt.broker.core.common.AbstractAsyncUserProcessor;
import com.iohao.game.bolt.broker.core.common.IoGameGlobalConfig;
import com.iohao.game.bolt.broker.server.BrokerServer;
import com.iohao.game.bolt.broker.server.aware.BrokerServerAware;
import com.iohao.game.bolt.broker.server.balanced.BalancedManager;
import com.iohao.game.bolt.broker.server.balanced.ExternalBrokerClientLoadBalanced;
import com.iohao.game.bolt.broker.server.balanced.region.BrokerClientProxy;
import com.iohao.game.common.kit.log.IoGameLoggerFactory;
import lombok.Setter;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * 把逻辑服的响应转发到对外服
 *
 * @author 渔民小镇
 * @date 2022-05-14
 */
public class ResponseMessageBrokerProcessor extends AbstractAsyncUserProcessor<ResponseMessage>
        implements BrokerServerAware {
    static final Logger log = IoGameLoggerFactory.getLoggerMsg();

    @Setter
    BrokerServer brokerServer;

    @Override
    public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, ResponseMessage responseMessage) {
        if (IoGameGlobalConfig.requestResponseLog) {
            log.info("把逻辑服的响应转发到对外服 {}", responseMessage);
        }

        HeadMetadata headMetadata = responseMessage.getHeadMetadata();
        int sourceClientId = headMetadata.getSourceClientId();

        BalancedManager balancedManager = brokerServer.getBalancedManager();
        ExternalBrokerClientLoadBalanced externalLoadBalanced = balancedManager.getExternalLoadBalanced();
        BrokerClientProxy brokerClientProxy = externalLoadBalanced.get(sourceClientId);

        if (Objects.isNull(brokerClientProxy)) {
            log.warn("对外服不存在: [{}]", sourceClientId);
            return;
        }

        try {
            // 转发 给 对外服务器
            brokerClientProxy.oneway(responseMessage);
        } catch (RemotingException | InterruptedException e) {
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
        return ResponseMessage.class.getName();
    }
}
