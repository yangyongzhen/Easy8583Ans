package com.example.yang.myapplication.rxbus;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * PracticeRxBus
 *
 * @author 王文彬
 * @date 2017/12/25
 */
public class RxBus {

  private Relay<Object> rxBus = null;

  private static RxBus instance;

  private static class IRxBusHolder {
    private static final RxBus INSTANCE = new RxBus();
  }

  private RxBus() {
    rxBus = BehaviorRelay.create().toSerialized();
  }

  public static final RxBus getInstance() {
    return IRxBusHolder.INSTANCE;
  }

  public void post(Object event) {
    rxBus.accept(event);
  }

  private <T> Observable<T> register(Class<T> eventType) {
    return rxBus.ofType(eventType);
  }

  public <T> Disposable toObservable(Class<T> eventType, Consumer<T> onNext) {
    return register(eventType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext);
  }

  public <T> Disposable toObservable(
      Class<T> eventType, Scheduler subScheduler, Scheduler obsScheduler, Consumer<T> onNext) {
    return register(eventType).subscribeOn(subScheduler).observeOn(obsScheduler).subscribe(onNext);
  }

  public <T> Disposable toObservable(Class<T> eventType, Consumer<T> onNext, Consumer onError) {
    return register(eventType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError);
  }

  public <T> Disposable toObservable(
      Class<T> eventType,
      Scheduler subScheduler,
      Scheduler obsScheduler,
      Consumer<T> onNext,
      Consumer onError) {
    return register(eventType)
        .subscribeOn(subScheduler)
        .observeOn(obsScheduler)
        .subscribe(onNext, onError);
  }

  public <T> Disposable toObservable(
      Class<T> eventType, Consumer<T> onNext, Consumer onError, Action onComplete) {
    return register(eventType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError, onComplete);
  }

  public <T> Disposable toObservable(
      Class<T> eventType,
      Scheduler subScheduler,
      Scheduler obsScheduler,
      Consumer<T> onNext,
      Consumer onError,
      Action onComplete) {
    return register(eventType)
        .subscribeOn(subScheduler)
        .observeOn(obsScheduler)
        .subscribe(onNext, onError, onComplete);
  }

  public <T> Disposable toObservable(
      Class<T> eventType,
      Consumer<T> onNext,
      Consumer onError,
      Action onComplete,
      Consumer onSubscribe) {
    return register(eventType)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onNext, onError, onComplete, onSubscribe);
  }

  public <T> Disposable toObservable(
      Class<T> eventType,
      Scheduler subScheduler,
      Scheduler obsScheduler,
      Consumer<T> onNext,
      Consumer onError,
      Action onComplete,
      Consumer onSubscribe) {
    return register(eventType)
        .subscribeOn(subScheduler)
        .observeOn(obsScheduler)
        .subscribe(onNext, onError, onComplete, onSubscribe);
  }

  public boolean isObserver() {
    return rxBus.hasObservers();
  }

  public void unregister(Disposable disposable) {
    if (disposable != null && !disposable.isDisposed()) {
      disposable.dispose();
    }
  }
}
