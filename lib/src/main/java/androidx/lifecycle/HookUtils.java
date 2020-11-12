package androidx.lifecycle;

import com.example.livedatabus.BuildConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author puppet
 * @date 2020/11/12 0012 14:12
 */
class HookUtils {
    public static void alignVersion(LiveData liveData, Observer observer) {
        Class<LiveData> liveDataClass = LiveData.class;
        try {
            //获取field private SafeIterableMap<Observer<T>, ObserverWrapper> mObservers
            Field mObservers = liveDataClass.getDeclaredField("mObservers");
            mObservers.setAccessible(true);

            //获取SafeIterableMap集合mObservers
            Object observers = mObservers.get(liveData);

            //获取SafeIterableMap的get(Object obj)方法
            Class<?> observersClass = observers.getClass();
            Method methodGet = observersClass.getDeclaredMethod("get", Object.class);
            methodGet.setAccessible(true);

            //获取到observer在集合中对应的ObserverWrapper对象
            Object objectWrapperEntry = methodGet.invoke(observers, observer);
            Object objectWrapper = null;
            if (objectWrapperEntry instanceof Map.Entry) {
                objectWrapper = ((Map.Entry) objectWrapperEntry).getValue();
            }
            if (objectWrapper == null) {
                throw new NullPointerException("ObserverWrapper can not be null");
            }

            //获取ObserverWrapper的Class对象  LifecycleBoundObserver extends ObserverWrapper
            Class<?> wrapperClass = objectWrapper.getClass().getSuperclass();

            //获取ObserverWrapper的field mLastVersion
            Field mLastVersion = wrapperClass.getDeclaredField("mLastVersion");
            mLastVersion.setAccessible(true);

            //把当前ListData的mVersion赋值给 ObserverWrapper的field mLastVersion
            mLastVersion.set(objectWrapper, liveData.getVersion());
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                throw new RuntimeException(e);
            } else {
                e.printStackTrace();
            }
        }
    }
}
