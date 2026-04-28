package com.hmdp.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScrollResult<T> {
    List<T> list;
    Long lastId;
    Integer offset;
    Boolean hasMore;
}
