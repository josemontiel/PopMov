package com.engtoolsdev.popmov.utils;

import android.util.LruCache;

import java.util.HashMap;

import rx.Observable;
import rx.Subscription;

/**
 * Created by Jose on 6/20/15.
 */
public class SubscriptionCache{

    private static SubscriptionCache subscriptionCache;
    private HashMap<String, Observable> subscriptionMap;


    public static SubscriptionCache getInstance() {
        if(subscriptionCache == null){
            subscriptionCache = new SubscriptionCache();
        }
        return subscriptionCache;
    }

    public SubscriptionCache(){
        subscriptionMap = new HashMap<>();
    }

    public void put(String key, Observable sub){
        subscriptionMap.put(key, sub);
    }

    public Observable get(String key){
        return subscriptionMap.get(key);
    }

    public boolean contains(String key){
        return subscriptionMap.containsKey(key);
    }

    public Observable remove(String key){
        return subscriptionMap.remove(key);
    }


}
