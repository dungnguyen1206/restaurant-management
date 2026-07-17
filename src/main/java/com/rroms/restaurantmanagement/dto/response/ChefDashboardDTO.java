package com.rroms.restaurantmanagement.dto.response;

import com.rroms.restaurantmanagement.entity.Order;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChefDashboardDTO {
    private long pendingCount;
    private long preparingCount;
    private long completedCountToday;
    private String avgCookingTimeToday;
    private List<PopularMenuItemDTO> popularItems;
    private List<Order> latestOrders;
}
