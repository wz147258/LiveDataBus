package com.example.livedatabus;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.StickyLiveData;
import androidx.lifecycle.UnstickyLiveData;

import java.util.HashMap;
import java.util.Map;

public class LiveDataBus {
    public static class SingletonHolder {
        private static final LiveDataBus instance = new LiveDataBus();
    }

    public static LiveDataBus getInstance() {
        return SingletonHolder.instance;
    }

    private final Map<Class<?>, MutableLiveData> liveDataMap = new HashMap<>();
    private final Map<Class<?>, MutableLiveData> stickyLiveDataMap = new HashMap<>();

    private final Object lock = new Object();

    private LiveDataBus() {
    }

    public void post(Object o) {
        MutableLiveData liveData = getOrCreate(o.getClass(), false);
        if (isMainThread()) {
            liveData.setValue(o);
        } else {
            liveData.postValue(o);
        }
    }

    public <T> LiveData<T> toObserve(Class<T> c) {
        return getOrCreate(c, false);
    }

    public void postSticky(Object o) {
        MutableLiveData stickySource = getOrCreate(o.getClass(), true);
        if (isMainThread()) {
            stickySource.setValue(o);
        } else {
            stickySource.postValue(o);
        }
    }

    public <T> LiveData<T> toObserveSticky(Class<T> c) {
        return getOrCreate(c, true);
    }

    public boolean removeStickyEvent(Class<?> c) {
        final StickyLiveData stickyLiveData = (StickyLiveData) get(c, true);
        if (stickyLiveData != null) {
            stickyLiveData.removeStickyEvent();
            return true;
        }
        return false;
    }

    private MutableLiveData getOrCreate(Class<?> c, boolean isSticky) {
        MutableLiveData result = get(c, isSticky);
        if (result == null) {
            synchronized (lock) {
                if (isSticky) {
                    result = new StickyLiveData();
                    stickyLiveDataMap.put(c, result);
                } else {
                    result = new UnstickyLiveData();
                    liveDataMap.put(c, result);
                }
            }
        }
        return result;
    }

    private MutableLiveData get(Class<?> c, boolean isSticky) {
        MutableLiveData result = null;
        synchronized (lock) {
            if (isSticky) {
                result = stickyLiveDataMap.get(c);
            } else {
                result = liveDataMap.get(c);
            }
        }
        return result;
    }

    public static boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }
}
