package com.example.yang.myapplication.rxbus;

import android.annotation.SuppressLint;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * RxBus
 * @author CX
 */
public class RxBus {
  @SuppressLint("NewApi")
  private ArrayMap<Object, List<Subject>> maps = new ArrayMap<Object, List<Subject>>();
  private static volatile RxBus instance;

  private RxBus() {
  }

  public static RxBus get() {
    if (instance == null) {
      synchronized (RxBus.class) {
        if (instance == null) {
          instance = new RxBus();
        }
      }
    }
    return instance;
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("unchecked")
  public <T> Observable<T> register(Object tag, Class<T> clazz) {
    List<Subject> subjects = maps.get(tag);
    if (subjects == null) {
      subjects = new ArrayList<Subject>();
      maps.put(tag, subjects);
    }
    Subject<T> subject = PublishSubject.<T> create();
    subjects.add(subject);
    return subject;
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("unchecked")
  public void unregister(Object tag, Observable observable) {
    List<Subject> subjects = maps.get(tag);
    if (subjects != null) {
      subjects.remove((Subject) observable);
      if (subjects.isEmpty()) {
        maps.remove(tag);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void post(Object o) {
    post(o.getClass().getSimpleName(), o);
  }

  @SuppressLint("NewApi")
  @SuppressWarnings("unchecked")
  public void post(Object tag, Object o) {
    List<Subject> subjects = maps.get(tag);
    if (subjects != null && !subjects.isEmpty()) {
      Iterator<Subject> iterator = subjects.iterator();
      while (iterator.hasNext()) {
        Subject subject = iterator.next();
        subject.onNext(o);
      }
      /*
       * for (Subject s : subjects) { s.onNext(o); }
       */
    }
  }

  @SuppressLint("NewApi")
  public void clearByTag(Object tag) {
    if(maps.containsKey(tag)) {
      List<Subject> subjects = maps.get(tag);
      if (subjects != null) {
        subjects.clear();
      }
    }
  }

}
