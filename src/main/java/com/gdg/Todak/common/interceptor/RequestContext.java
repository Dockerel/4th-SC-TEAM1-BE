package com.gdg.Todak.common.interceptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class RequestContext {
    private String httpMethod;
    private String bestMatchPath;
    private final Map<QueryType, Integer> queryCountByType = new HashMap<>();

    public void incrementQueryCount(String sql) {
        QueryType queryType = QueryType.from(sql);
        queryCountByType.merge(queryType, 1, Integer::sum);
    }
}
