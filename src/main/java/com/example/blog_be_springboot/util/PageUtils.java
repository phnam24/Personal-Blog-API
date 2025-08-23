package com.example.blog_be_springboot.util;


import org.springframework.data.domain.*;
import java.util.*;

public final class PageUtils {
    private PageUtils() {}

    public static Pageable sanitize(Pageable in, int defaultSize, int maxSize, List<String> allowedSorts) {
        int page = Math.max(in.getPageNumber(), 0);
        int size = in.getPageSize() <= 0 ? defaultSize : Math.min(in.getPageSize(), maxSize);

        Sort sort = in.getSort();
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Order.desc("updated_at"));
        } else {
            List<Sort.Order> safeOrders = new ArrayList<>();
            for (Sort.Order o : sort) {
                if (allowedSorts.contains(o.getProperty())) {
                    safeOrders.add(o);
                }
            }
            sort = safeOrders.isEmpty() ? Sort.by(Sort.Order.desc("updated_at")) : Sort.by(safeOrders);
        }
        return PageRequest.of(page, size, sort);
    }
}
