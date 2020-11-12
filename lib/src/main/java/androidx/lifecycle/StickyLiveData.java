package androidx.lifecycle;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import static androidx.lifecycle.Lifecycle.State.STARTED;

/**
 * @author puppet
 * @date 2020/2/25 0025 13:34
 */
public class StickyLiveData<T> extends MutableLiveData<T> {

    private volatile boolean removedStickyEvent = false;

    private final Map<Observer<? super T>, PreventStickyEventObserverWrapper<? super T>> myObserverWrapperMap = new HashMap<>();

    public StickyLiveData(T value) {
        super(value);
    }

    public StickyLiveData() {
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        assertMainThread("observe");

        if (removedStickyEvent && getVersion() > LiveData.START_VERSION) {//有新值，会调用onChanged
            if (owner.getLifecycle().getCurrentState().isAtLeast(STARTED)) {//此时super.observe内会立马调用onChanged
                PreventStickyEventObserverWrapper<? super T> myObserverWrapper = myObserverWrapperMap.get(observer);
                if (myObserverWrapper == null) {
                    myObserverWrapper = new PreventStickyEventObserverWrapper<>(observer);
                    myObserverWrapper.preventStickyEvent = true;
                    myObserverWrapperMap.put(observer, myObserverWrapper);
                    super.observe(owner, myObserverWrapper);
                }

            } else {
                super.observe(owner, observer);
                HookUtils.alignVersion(this, observer);
            }
        } else {
            super.observe(owner, observer);
        }
    }

    @Override
    public void observeForever(@NonNull Observer<? super T> observer) {
        assertMainThread("observeForever");
        if (removedStickyEvent) {
            PreventStickyEventObserverWrapper<? super T> myObserverWrapper = myObserverWrapperMap.get(observer);
            if (myObserverWrapper == null) {
                myObserverWrapper = new PreventStickyEventObserverWrapper<>(observer);
                myObserverWrapper.preventStickyEvent = true;
                myObserverWrapperMap.put(observer, myObserverWrapper);
                super.observeForever(myObserverWrapper);
            }
        } else {
            super.observeForever(observer);
        }
    }

    @Override
    public void removeObserver(@NonNull Observer<? super T> observer) {
        assertMainThread("removeObserver");

        if (observer instanceof PreventStickyEventObserverWrapper) {// 从liveData内部删除
            for (Map.Entry<Observer<? super T>, PreventStickyEventObserverWrapper<? super T>> entry : myObserverWrapperMap.entrySet()) {
                if (entry.getValue() == observer) {
                    myObserverWrapperMap.remove(entry.getKey());
                    break;
                }
            }
            super.removeObserver(observer);

        } else {
            PreventStickyEventObserverWrapper<? super T> myObserverWrapper = myObserverWrapperMap.remove(observer);
            if (myObserverWrapper != null) {
                super.removeObserver(myObserverWrapper);
            } else {
                super.removeObserver(observer);
            }
        }
    }

    @Override
    public void setValue(T value) {
        assertMainThread("setValue");
        removedStickyEvent = false;
        super.setValue(value);
    }

    public void removeStickyEvent() {
        removedStickyEvent = true;
    }
}
