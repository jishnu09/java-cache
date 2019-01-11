/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcg.mcube.cachemap;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SwarnenduS
 */
public class CacheHolder {

    private final Logger log = LoggerFactory.getLogger(CacheHolder.class);

    private static final Map<String, CacheObject> cacheMap = new LinkedHashMap<>();

    private static final CacheHolder cacheHolder = new CacheHolder();

    private CacheHolder() {
        //load objects
    }

    public static CacheHolder getInstance() {
        return cacheHolder;
    }

    public void putValue(String key, Object value, int timeToLive) {
        synchronized (cacheMap) {
            if (cacheMap.containsKey(key)) {
                log.info("Overwritting: " + key);
                CacheObject existingObj = cacheMap.get(key);
                CacheObject obj = new CacheObject(value, existingObj.getLastAccessed(), Calendar.getInstance().getTimeInMillis(), timeToLive);
                cacheMap.put(key, obj);
            } else {
                log.info("Creating: " + key);
                CacheObject obj = new CacheObject(value, 0L, timeToLive);
                cacheMap.put(key, obj);
            }
        }
    }

    public Object getValue(String key) {
        synchronized (cacheMap) {
            CacheObject obj = cacheMap.get(key);
            log.info("Last Accessed: " + (obj.getLastAccessed() == 0 ? "Never" : obj.getLastAccessed()));
            log.info("Last Modified: " + (obj.getLastModified() == 0 ? "Never" : obj.getLastModified()));
            obj.setLastAccessed(Calendar.getInstance().getTimeInMillis());
            return obj.getValue();
        }
    }

    public Map<String, CacheObject> getAll() {
        synchronized (cacheMap) {
            Iterator<String> keys = cacheMap.keySet().iterator();
            log.info("Cache Size: " + cacheMap.size());
            while (keys.hasNext()) {
                String key = keys.next();
                CacheObject obj = (CacheObject) cacheMap.get(key);
                obj.setLastAccessed(Calendar.getInstance().getTimeInMillis());
            }

//            log.info("Last Accessed: " + (obj.getLastAccessed() == 0 ? "Never" : obj.getLastAccessed()));
//            log.info("Last Modified: " + (obj.getLastModified() == 0 ? "Never" : obj.getLastModified()));
            return cacheMap;
        }
    }

    public void cleanup() {
        // log.info("Electing keys for cleaning....");
        synchronized (cacheMap) {
//            Set<String> keys = cacheMap.keySet();
//            Long currentTime = Calendar.getInstance().getTimeInMillis();
//            for (String key : keys) {
//                CacheObject obj = (CacheObject) cacheMap.get(key);
//                if (currentTime > (obj.getLastAccessed() + obj.getTimeToLive())) {
//                    log.info("Removing: " + key);
//                    cacheMap.remove(key);
//                }
//            }
            Iterator<String> keys = cacheMap.keySet().iterator();
            Long currentTime = Calendar.getInstance().getTimeInMillis();
            log.info("Cache Size: " + cacheMap.size());
            List<String> removeList = new LinkedList<>();
            while (keys.hasNext()) {
                String key = keys.next();
                CacheObject obj = (CacheObject) cacheMap.get(key);
                if (obj.getTimeToLive() != -1) {
                    if (currentTime > (obj.getLastAccessed() + obj.getTimeToLive())) {
                        removeList.add(key);
                    }
                }
            }
            for (int i = 0; i < removeList.size(); i++) {
                log.info("Removing: " + removeList.get(i));
                cacheMap.remove(removeList.get(i));
                log.info("Removed");
            }
            removeList.clear();
        }
    }

    public static void main(String[] args) {

        cacheHolder.putValue("String_1", "abc", 100);
        cacheHolder.putValue("Int_1", 1, 200);
        cacheHolder.putValue("Long_1", 2L, 400);
        cacheHolder.log.info(cacheHolder.getAll() + "");
        cacheHolder.log.info(cacheHolder.getValue("String_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("Int_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("String_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("Long_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("Int_1") + "");

        cacheHolder.log.info("Again Putting");

        cacheHolder.putValue("String_1", "def", 300);
        cacheHolder.putValue("Int_1", 3, 100);
        cacheHolder.putValue("Long_1", 4L, 100);
        cacheHolder.putValue("infinite", 4.7, -1);

        cacheHolder.log.info(cacheHolder.getValue("String_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("Int_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("String_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("Long_1") + "");
        cacheHolder.log.info(cacheHolder.getValue("Int_1") + "");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100000);
                        cacheHolder.cleanup();
                    } catch (Exception ex) {
                        cacheHolder.log.error("", ex);
                    }

                }
            }
        });

        //t.setDaemon(true);
        t.start();
    }
}

class CacheObject {

    private Object value;
    private Long lastAccessed;
    private Long lastModified;
    private int timeToLive;

    public CacheObject(Object value, Long lastAccessed) {
        this.value = value;
        this.lastAccessed = lastAccessed;
        this.lastModified = 0L;
        this.timeToLive = -1;
    }

    public CacheObject(Object value, Long lastAccessed, int timeToLive) {
        this.value = value;
        this.lastAccessed = lastAccessed;
        this.lastModified = 0L;
        this.timeToLive = timeToLive == -1 ? timeToLive : (timeToLive * 1000);
    }

    public CacheObject(Object value, Long lastAccessed, Long lastModified, int timeToLive) {
        this.value = value;
        this.lastAccessed = lastAccessed;
        this.lastModified = lastModified;
        // this.timeToLive = (timeToLive * 1000);
        this.timeToLive = timeToLive == -1 ? timeToLive : (timeToLive * 1000);
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Long lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

}
