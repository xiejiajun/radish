package org.sam.shen.core.thread;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.sam.shen.core.agent.RadishAgent;
import org.sam.shen.core.event.HandlerEvent;
import org.sam.shen.core.model.Resp;
import org.sam.shen.core.rpc.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suoyao
 * @date 2018年8月1日 下午5:06:18
  *  触发任务线程
 */
public class TriggerEventThread {
	private static Logger logger = LoggerFactory.getLogger(TriggerEventThread.class);
	
	private static TriggerEventThread instance = new TriggerEventThread();

	public static TriggerEventThread getInstance() {
		return instance;
	}
	
	/**
	 * 触发抢占任务线程
	 */
	private Thread triggerThread;
	
	/**
	 *  任务执行线程
	 */
	private Thread callbckThread;
	
	private volatile boolean toStop = false;
	
	public void start(String rpcTriggerUrl, String rpcSubeventUrl, String rpcReportUrl, Long agentId) {
		triggerThread = new Thread(() -> {
            while(!toStop) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.info(" Callback Queue size is: {}", EventHandlerThreadPool.callbackQueueSize());
                    }
                    if (EventHandlerThreadPool.isCallbackQueueFull()) {
                        logger.error("Callback Queue is Full.");
                    } else {
                        Resp<List<HandlerEvent>> resp = RestRequest.getUriVariablesWithList(rpcTriggerUrl, HandlerEvent.class, agentId,
                                EventHandlerThreadPool.availableCount());
                        if(Resp.SUCCESS.getCode() == resp.getCode()) {
                            if(null != resp.getData()) {
                                for (HandlerEvent event : resp.getData()) {
                                    if (StringUtils.isNotEmpty(event.getEventId())) {
                                        EventHandlerThreadPool.pushCallbackQueue(event);
                                    }
                                }

                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(RadishAgent.getTriggerBeat());
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        });
		triggerThread.setDaemon(true);
		triggerThread.start();
		
		// 启动任务线程队列
		callbckThread = new Thread(() -> {
            while(!toStop) {
                if(EventHandlerThreadPool.isAvailable()) {
                    EventHandlerThreadPool.run(rpcSubeventUrl, rpcReportUrl);
                } else {
                    logger.error("CallbackThreadPool is Full.");
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(RadishAgent.getHandleEventBeat());
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        });
		callbckThread.setDaemon(true);
		callbckThread.start();
	}
	
	public void toStop() {
		toStop = true;
		// stop trigger & callback, interrupt and wait
		triggerThread.interrupt();
		callbckThread.interrupt();
		try {
			triggerThread.join();
			callbckThread.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
		}
	}
}
