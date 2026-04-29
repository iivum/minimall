package com.minimall.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minimall.domain.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper extends BaseMapper<Order> {

    @Select("""
        SELECT DATE(o.created_at) as date,
               COUNT(*) as orders,
               COALESCE(SUM(o.total_amount), 0) as gmv,
               COUNT(DISTINCT o.user_id) as users
        FROM orders o
        WHERE o.created_at >= #{startDate} AND o.created_at < #{endDate}
        GROUP BY DATE(o.created_at)
        ORDER BY date
        """)
    List<Map<String, Object>> getDailyMetrics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Select("""
        SELECT COUNT(*) as total_orders,
               COALESCE(SUM(total_amount), 0) as total_gmv,
               COUNT(DISTINCT user_id) as total_users,
               COALESCE(AVG(total_amount), 0) as avg_order_value
        FROM orders
        WHERE created_at >= #{startDate} AND created_at < #{endDate}
        """)
    Map<String, Object> getSummaryMetrics(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Select("""
        SELECT COUNT(*) FROM users
        WHERE created_at >= #{startDate} AND created_at < #{endDate}
        AND deleted_at IS NULL
        """)
    long countNewUsers(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Select("""
        SELECT DATE(created_at) as date, COUNT(*) as users
        FROM users
        WHERE created_at >= #{startDate} AND created_at < #{endDate}
        AND deleted_at IS NULL
        GROUP BY DATE(created_at)
        ORDER BY date
        """)
    List<Map<String, Object>> getDailyNewUsers(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Select("""
        SELECT COUNT(DISTINCT user_id)
        FROM orders
        WHERE DATE(created_at) = CURRENT_DATE
        """)
    long getTodayActiveUsers();

    @Select("""
        SELECT COUNT(DISTINCT user_id)
        FROM users
        WHERE DATE(created_at) = CURRENT_DATE
        AND deleted_at IS NULL
        """)
    long getTodayNewUsers();
}
