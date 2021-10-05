package net.lamgc.oracle.sentry.script.groovy.trigger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.requests.GetInstanceRequest;
import groovy.lang.Closure;
import net.lamgc.oracle.sentry.oci.compute.ComputeInstanceManager;
import net.lamgc.oracle.sentry.oci.compute.ComputeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 实例状态监视触发器.
 * <p> 通过轮询实例状态, 当状态为指定的状态时将触发回调执行操作.
 * <p> 吐槽: 换个地方轮询(xs).
 * @author LamGC
 */
@TriggerName("InstanceStateWatcher")
public class InstanceStateWatchTrigger implements GroovyTrigger {

    private final static Logger log = LoggerFactory.getLogger(InstanceStateWatchTrigger.class);
    private final static ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Thread-InstanceStatePolling-%d")
            .build();

    private final Set<ComputeInstance> instances = new HashSet<>();
    private final AtomicReference<ComputeInstanceManager> instanceManager = new AtomicReference<>();
    private final AtomicReference<Thread> pollingThreadReference = new AtomicReference<>();
    private final Set<Instance.LifecycleState> targetStates = new HashSet<>();
    private long interval = 5000;

    /**
     * 设置 {@link ComputeInstanceManager}.
     * <p> 设置计算实例管理器以实现部分功能.
     * @param instanceManager 实例管理器.
     */
    public void setInstanceManager(ComputeInstanceManager instanceManager) {
        this.instanceManager.set(instanceManager);
    }

    /**
     * 添加所有实例.
     * <p> 调用前需设置实例管理器.
     */
    public void allInstances() {
        addInstance(instanceManager.get().getComputeInstances());
    }

    /**
     * 添加需要监控的实例.
     * <p> 调用前需设置实例管理器.
     * @param instanceId 要添加的实例 Id.(注意不是实例名称.)
     */
    public void addInstance(String instanceId) {
        this.instances.add(instanceManager.get().getComputeInstanceById(instanceId));
    }

    /**
     * 添加需要监控的实例.
     * @param instance 实例对象.
     */
    public void addInstance(ComputeInstance instance) {
        this.instances.add(instance);
    }

    /**
     * 添加需要监控的实例.
     * @param instances 实例集合.
     */
    public void addInstance(Set<ComputeInstance> instances) {
        this.instances.addAll(instances);
    }

    /**
     * 设置监控间隔.
     * <p> 该间隔是每轮查询之间的间隔, 并非查询每个实例的间隔. Oracle 对该接口的请求限制很宽松, 且单个帐号的实例有限, 故不再设置查询间隔.
     * @param interval 间隔时常, 单位: 毫秒.
     */
    public void interval(long interval) {
        this.interval = interval;
    }

    /**
     * 要检查的状态.
     * <p> 当实例处于指定状态时将触发回调.
     * @param state 状态名.
     */
    public void state(String state) {
        targetStates.add(getState(state));
    }

    /**
     * 要检查的状态.
     * <p> 当实例处于指定状态时将触发回调.
     * @param states 状态集合.
     */
    public void state(String[] states) {
        Set<Instance.LifecycleState> stateSet = new HashSet<>();
        for (String state : states) {
            stateSet.add(getState(state));
        }
        targetStates.addAll(stateSet);
    }

    /**
     * 添加所有状态.
     * <p> 所有状态都会触发回调函数.
     */
    public void allStates() {
        targetStates.addAll(Set.of(Instance.LifecycleState.values()));
    }

    /**
     * 通过状态名获取对应的 {@link Instance.LifecycleState}.
     * @param state 状态名.
     * @return 返回对应值.
     * @throws IllegalArgumentException 当无法获取指定状态对象时抛出该异常.
     */
    private Instance.LifecycleState getState(String state) {
        try {
            return LifecycleStateMapping.valueOf(state.toUpperCase()).target;
        } catch (IllegalArgumentException e) {
            try {
                return Instance.LifecycleState.valueOf(toFirstCharUpperCase(state));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Invalid target state: " + state);
            }
        }
    }

    @Override
    public synchronized void run(final Closure<?> callback) {
        if (pollingThreadReference.get() != null) {
            throw new IllegalStateException("Attempting to start multiple check threads repeatedly, which is not allowed.");
        }
        if (targetStates.isEmpty()) {
            throw new IllegalArgumentException("The target status has not been set.");
        }
        if (instances.isEmpty()) {
            log.warn("尚未设置任何需监视的实例, 已跳过对触发器的启动.", new IllegalStateException("未设置任何需要监视的实例."));
            return;
        }

        Thread newPollingThread = THREAD_FACTORY.newThread(new PollingTask(instances, targetStates, callback, interval));
        newPollingThread.start();
        pollingThreadReference.set(newPollingThread);
    }

    @Override
    public synchronized void shutdown() {
        if (pollingThreadReference.get() == null) {
            return;
        }
        // 合规的，用于停止线程的方法。（笑）（无歧义）
        Thread pollingThread = pollingThreadReference.get();
        pollingThread.interrupt();
        pollingThreadReference.set(null);
    }

    /**
     * 首字母大写.
     * @param str 要处理的单词.
     * @return 如果首字母非大写, 就转换成大写.
     */
    private static String toFirstCharUpperCase(String str) {
        char firstChar = str.charAt(0);
        if (!Character.isUpperCase(firstChar)) {
            return Character.toUpperCase(firstChar) + str.substring(1);
        }
        return str;
    }

    /**
     * 轮询任务.
     */
    private static class PollingTask implements Runnable {

        private final Set<ComputeInstance> instances;
        private final Set<Instance.LifecycleState> targetStates;
        private final Closure<?> callback;
        private final long interval;
        private final Map<ComputeInstance, Instance.LifecycleState> lastStateMap = new ConcurrentHashMap<>();

        private PollingTask(Set<ComputeInstance> instances, Set<Instance.LifecycleState> targetStates,
                            Closure<?> callback, long interval) {
            this.instances = instances;
            this.targetStates = targetStates;
            this.callback = callback;
            if (interval <= 0) {
                throw new IllegalArgumentException("Interval cannot be less than or equal to 0.");
            }
            this.interval = interval;
        }

        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    // 并非 BusyWait, 而是延迟操作.
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    break;
                }
                log.trace("正在开始新一轮实例状态检查...");
                for (ComputeInstance instance : instances) {
                    ComputeClient computeClient = instance.getFromAccount().clients().compute();
                    Instance instanceInfo;
                    try {
                        instanceInfo = computeClient.getInstance(GetInstanceRequest.builder()
                                .instanceId(instance.getInstanceId())
                                .build()).getInstance();
                    } catch (Exception e) {
                        if (e.getCause() instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        if (log.isDebugEnabled()) {
                            log.error("获取实例状态失败.", e);
                        } else {
                            log.error("获取实例状态失败(详情请打开 Debug 级别): {}:{}", e.getClass().getName(), e.getMessage());
                        }
                        continue;
                    }
                    Instance.LifecycleState lastState = lastStateMap.get(instance);
                    if (targetStates.contains(instanceInfo.getLifecycleState()) &&
                            instanceInfo.getLifecycleState() != lastState) {
                        try {
                            callback.call(new InstanceStateChangeEvent(instance,
                                    lastState != null ? lastState.name() : null,
                                    instanceInfo.getLifecycleState().name()));
                        } catch (Exception e) {
                            log.error("实例状态事件处理时发生未捕获异常.", e);
                        }
                    }
                    lastStateMap.put(instance, instanceInfo.getLifecycleState());
                }
                log.trace("实例状态检查已结束.");
            }
        }
    }

    /**
     * 一个 Oracle 实例状态枚举的映射枚举类.
     */
    @SuppressWarnings({"AlibabaEnumConstantsMustHaveComment", "unused"})
    private enum LifecycleStateMapping {
        MOVING(Instance.LifecycleState.Moving),
        PROVISIONING(Instance.LifecycleState.Provisioning),
        RUNNING(Instance.LifecycleState.Running),
        STARTING(Instance.LifecycleState.Starting),
        STOPPING(Instance.LifecycleState.Stopping),
        STOPPED(Instance.LifecycleState.Stopped),
        CREATING_IMAGE(Instance.LifecycleState.CreatingImage),
        TERMINATING(Instance.LifecycleState.Terminating),
        TERMINATED(Instance.LifecycleState.Terminated);

        private final Instance.LifecycleState target;

        LifecycleStateMapping(Instance.LifecycleState target) {
            this.target = target;
        }
    }

    /**
     * 实例状态变更事件.
     * @param instance 相关实例.
     * @param oldState 旧状态, 当监视器第一次查询即触发时, 该值为 {@code null}.
     * @param newState 新状态.
     */
    public static record InstanceStateChangeEvent(
            ComputeInstance instance,
            String oldState,
            String newState
    ) {}

}
