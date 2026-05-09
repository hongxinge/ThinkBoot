package com.thinkboot.core.converter;

import java.util.List;

/**
 * MapStruct 基础转换器接口
 * 提供对象和集合转换的通用方法
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 */
public interface BaseConverter<S, T> {

    /**
     * 源对象转目标对象
     */
    T to(S source);

    /**
     * 目标对象转源对象
     */
    S from(T target);

    /**
     * 源对象列表转目标对象列表
     */
    List<T> toList(List<S> sourceList);

    /**
     * 目标对象列表转源对象列表
     */
    List<S> fromList(List<T> targetList);
}
