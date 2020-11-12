package androidx.lifecycle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class PreventStickyEventObserverWrapper<T> implements Observer<T> {

    @NonNull
    final Observer<T> observer;
    boolean preventStickyEvent = false;

    PreventStickyEventObserverWrapper(@NonNull Observer<T> observer) {
        this.observer = observer;
    }

    @Override
    public void onChanged(@Nullable T t) {
        if (preventStickyEvent) {
            preventStickyEvent = false;
            return;
        }
        observer.onChanged(t);
    }
}