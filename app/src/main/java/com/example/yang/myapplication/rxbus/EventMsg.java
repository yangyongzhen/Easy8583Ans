package com.example.yang.myapplication.rxbus;

import java.util.List;

/**
 * PracticeRxBus
 *
 * @author 王文彬
 * @date 2017/12/25
 */
public class EventMsg<T> {

  private T event;

  private List<?> list;

  private String tag;

  public EventMsg(T event) {
    this.event = event;
  }

  public EventMsg(String tag, T event) {
    this.tag = tag;
    this.event = event;
  }

  public EventMsg(List<?> list) {
    this.list = list;
  }

  public EventMsg(String tag, List<?> list) {
    this.tag = tag;
    this.list = list;
  }

  public String getTag() {
    return tag;
  }

  public T getData() {
    return event;
  }

  public List<?> getList() {
    return list;
  }
}


