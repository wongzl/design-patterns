package com.zerowzl.collection;

import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 接口信息采集。
 *
 * @author wangzhiliang
 * @version v1.0.0
 * @since 2019-12-30 15:19
 */

public class ApiCollection {

    /**
     * key是接口名称, value是接口对应的相应时间或者时间戳
     */
    private Map<String, List<Double>> responseTimes = new ConcurrentHashMap<>();
    private Map<String, List<Double>> timesTemps = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws InterruptedException {
        ApiCollection apiCollection = new ApiCollection();
        apiCollection.startRepeatedReport(10, TimeUnit.SECONDS);
        for (int i = 0; i < 10000000; i++) {
            long timesTemp = System.currentTimeMillis();
            apiCollection.recordTimesTemps("test1", timesTemp);
            Thread.sleep(100);
            apiCollection.recordResponseTimes("test1", System.currentTimeMillis() - timesTemp);
        }
    }

    public void recordResponseTimes(String apiName, double responseTime) {
        responseTimes.putIfAbsent(apiName, new ArrayList<>());
        responseTimes.get(apiName).add(responseTime);
    }

    public void recordTimesTemps(String apiName, double timesTemp) {
        timesTemps.putIfAbsent(apiName, new ArrayList<>());
        timesTemps.get(apiName).add(timesTemp);
    }

    public void startRepeatedReport(long period, TimeUnit timeUnit) {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Map<String, Map<String, Double>> state = new HashMap<>(16);
                for (Entry<String, List<Double>> entry : responseTimes.entrySet()) {
                    String apiName = entry.getKey();
                    List<Double> apiResponseTimes = entry.getValue();
                    state.putIfAbsent(apiName, new HashMap<>(16));
                    Map<String, Double> map = state.get(apiName);
                    map.put("max", max(apiResponseTimes));
                    map.put("min", min(apiResponseTimes));
                }

                for (Entry<String, List<Double>> entry : timesTemps.entrySet()) {
                    String key = entry.getKey();
                    List<Double> apiTimesTemps = entry.getValue();
                    state.putIfAbsent(key, new HashMap<>(16));
                    Map<String, Double> map = state.get(key);
                    map.put("count", (double) apiTimesTemps.size());
                }

                System.out.println(JSON.toJSONString(state));
            }
        }, 0, period, timeUnit);
    }

    private double max(List<Double> args) {
        return Collections.max(args);
    }

    private double min(List<Double> args) {
        return Collections.min(args);
    }

}
