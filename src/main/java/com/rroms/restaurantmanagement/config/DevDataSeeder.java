package com.rroms.restaurantmanagement.config;

import com.rroms.restaurantmanagement.entity.Category;
import com.rroms.restaurantmanagement.entity.MenuItem;
import com.rroms.restaurantmanagement.entity.Order;
import com.rroms.restaurantmanagement.entity.OrderItem;
import com.rroms.restaurantmanagement.entity.Reservation;
import com.rroms.restaurantmanagement.entity.ReservationTable;
import com.rroms.restaurantmanagement.entity.RestaurantTable;
import com.rroms.restaurantmanagement.entity.Role;
import com.rroms.restaurantmanagement.entity.User;
import com.rroms.restaurantmanagement.entity.constant.OrderItemStatus;
import com.rroms.restaurantmanagement.entity.constant.OrderStatus;
import com.rroms.restaurantmanagement.entity.constant.ReservationStatus;
import com.rroms.restaurantmanagement.entity.constant.RoleName;
import com.rroms.restaurantmanagement.entity.constant.TableStatus;
import com.rroms.restaurantmanagement.entity.constant.UserStatus;
import com.rroms.restaurantmanagement.repository.CategoryRepository;
import com.rroms.restaurantmanagement.repository.MenuItemRepository;
import com.rroms.restaurantmanagement.repository.OrderRepository;
import com.rroms.restaurantmanagement.repository.ReservationRepository;
import com.rroms.restaurantmanagement.repository.RoleRepository;
import com.rroms.restaurantmanagement.repository.TableRepository;
import com.rroms.restaurantmanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "123456";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Map<RoleName, Role> roles = seedRoles();
        Map<String, User> users = seedUsers(roles);
        Map<String, Category> categories = seedCategories();
        Map<String, MenuItem> menuItems = seedMenuItems(categories);
        Map<String, RestaurantTable> tables = seedTables(users);
        seedReservations(users, tables);
        seedOrders(users, tables, menuItems);
    }

    private Map<RoleName, Role> seedRoles() {
        Map<RoleName, Role> roles = new HashMap<>();

        for (RoleName roleName : RoleName.values()) {
            Role role = roleRepository.findByRoleName(roleName);
            if (role == null) {
                role = roleRepository.save(Role.builder()
                        .roleName(roleName)
                        .description(roleName.name() + " demo role")
                        .build());
            }
            roles.put(roleName, role);
        }

        return roles;
    }

    private Map<String, User> seedUsers(Map<RoleName, Role> roles) {
        Map<String, User> users = new HashMap<>();

        putUser(users, "admin", "Admin", "", "User", "0900000001", roles.get(RoleName.ADMIN));
        putUser(users, "manager", "Manager", "", "User", "0900000002", roles.get(RoleName.MANAGER));
        putUser(users, "receptionist", "Receptionist", "", "User", "0900000003", roles.get(RoleName.RECEPTIONIST));
        putUser(users, "waiter", "Waiter", "", "One", "0900000004", roles.get(RoleName.WAITER));
        putUser(users, "waiter2", "Waiter", "", "Two", "0900000005", roles.get(RoleName.WAITER));
        putUser(users, "chef", "Chef", "", "User", "0900000006", roles.get(RoleName.CHEF));
        putUser(users, "customer", "Customer", "", "User", "0900000007", roles.get(RoleName.CUSTOMER));

        return users;
    }

    private void putUser(Map<String, User> users, String username, String firstName, String middleName,
                         String lastName, String phone, Role role) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user == null) {
            user = userRepository.save(User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .firstName(firstName)
                    .middleName(middleName)
                    .lastName(lastName)
                    .phone(phone)
                    .status(UserStatus.ACTIVE)
                    .role(role)
                    .build());
        }
        users.put(username, user);
    }

    private Map<String, Category> seedCategories() {
        Map<String, Category> categories = new HashMap<>();
        for (Category category : categoryRepository.findAll()) {
            categories.put(category.getCategoryName().toLowerCase(), category);
        }

        putCategory(categories, "Appetizers");
        putCategory(categories, "Main Courses");
        putCategory(categories, "Noodles and Rice");
        putCategory(categories, "Drinks");
        putCategory(categories, "Desserts");

        return categories;
    }

    private void putCategory(Map<String, Category> categories, String name) {
        String key = name.toLowerCase();
        if (!categories.containsKey(key)) {
            categories.put(key, categoryRepository.save(Category.builder()
                    .categoryName(name)
                    .build()));
        }
    }

    private Map<String, MenuItem> seedMenuItems(Map<String, Category> categories) {
        Map<String, MenuItem> menuItems = new HashMap<>();
        for (MenuItem menuItem : menuItemRepository.findAll()) {
            menuItems.put(menuItem.getItemName().toLowerCase(), menuItem);
        }

        putMenuItem(menuItems, "Spring Rolls", "Crispy rolls with vegetables", "Appetizers", "59000", 40, categories);
        putMenuItem(menuItems, "Chicken Wings", "Garlic butter fried wings", "Appetizers", "89000", 30, categories);
        putMenuItem(menuItems, "Beef Steak", "Grilled beef with pepper sauce", "Main Courses", "249000", 18, categories);
        putMenuItem(menuItems, "Grilled Salmon", "Salmon with lemon butter sauce", "Main Courses", "229000", 16, categories);
        putMenuItem(menuItems, "Seafood Fried Rice", "Fried rice with shrimp and squid", "Noodles and Rice", "129000", 25, categories);
        putMenuItem(menuItems, "Beef Pho", "Rice noodles with beef broth", "Noodles and Rice", "89000", 35, categories);
        putMenuItem(menuItems, "Iced Tea", "House iced tea", "Drinks", "25000", 100, categories);
        putMenuItem(menuItems, "Orange Juice", "Fresh orange juice", "Drinks", "45000", 60, categories);
        putMenuItem(menuItems, "Tiramisu", "Coffee mascarpone dessert", "Desserts", "69000", 20, categories);
        putMenuItem(menuItems, "Cheesecake", "New York style cheesecake", "Desserts", "79000", 18, categories);

        return menuItems;
    }

    private void putMenuItem(Map<String, MenuItem> menuItems, String name, String description, String categoryName,
                             String price, Integer stock, Map<String, Category> categories) {
        String key = name.toLowerCase();
        if (!menuItems.containsKey(key)) {
            MenuItem menuItem = menuItemRepository.save(MenuItem.builder()
                    .itemName(name)
                    .description(description)
                    .price(new BigDecimal(price))
                    .imageUrl("https://placehold.co/600x400?text=" + name.replace(" ", "+"))
                    .isSoldOut(false)
                    .virtualInStock(stock)
                    .category(categories.get(categoryName.toLowerCase()))
                    .build());
            menuItems.put(key, menuItem);
        }
    }

    private Map<String, RestaurantTable> seedTables(Map<String, User> users) {
        Map<String, RestaurantTable> tables = new HashMap<>();
        for (RestaurantTable table : tableRepository.findAll()) {
            tables.put(table.getTableNumber(), table);
        }

        putTable(tables, "A01", 2, "Indoor", TableStatus.AVAILABLE, users.get("waiter"));
        putTable(tables, "A02", 4, "Indoor", TableStatus.RESERVED, users.get("waiter"));
        putTable(tables, "A03", 4, "Indoor", TableStatus.OCCUPIED, users.get("waiter"));
        putTable(tables, "A04", 6, "Indoor", TableStatus.AVAILABLE, users.get("waiter"));
        putTable(tables, "B01", 2, "Window", TableStatus.AVAILABLE, users.get("waiter2"));
        putTable(tables, "B02", 4, "Window", TableStatus.RESERVED, users.get("waiter2"));
        putTable(tables, "B03", 6, "Window", TableStatus.AVAILABLE, users.get("waiter2"));
        putTable(tables, "C01", 8, "VIP", TableStatus.RESERVED, users.get("waiter"));
        putTable(tables, "C02", 10, "VIP", TableStatus.AVAILABLE, users.get("waiter2"));
        putTable(tables, "D01", 4, "Outdoor", TableStatus.OCCUPIED, users.get("waiter"));
        putTable(tables, "D02", 4, "Outdoor", TableStatus.AVAILABLE, users.get("waiter2"));
        putTable(tables, "M01", 4, "Maintenance", TableStatus.OUT_OF_SERVICE, users.get("waiter2"));

        return tables;
    }

    private void putTable(Map<String, RestaurantTable> tables, String tableNumber, Integer capacity, String area,
                          TableStatus status, User assignedWaiter) {
        if (!tables.containsKey(tableNumber)) {
            RestaurantTable table = tableRepository.save(RestaurantTable.builder()
                    .tableNumber(tableNumber)
                    .capacity(capacity)
                    .area(area)
                    .status(status)
                    .assignedWaiter(assignedWaiter)
                    .createdAt(LocalDateTime.now())
                    .createdBy("dev-seeder")
                    .build());
            tables.put(tableNumber, table);
        }
    }

    private void seedReservations(Map<String, User> users, Map<String, RestaurantTable> tables) {
        boolean alreadySeeded = reservationRepository.findAll().stream()
                .anyMatch(reservation -> reservation.getNote() != null
                        && reservation.getNote().startsWith("Seed reservation"));
        if (alreadySeeded) {
            return;
        }

        createReservation("Nguyen Minh Anh", "0911111111", 2, ReservationStatus.PENDING,
                LocalDateTime.now().plusHours(2), "Seed reservation - pending online booking",
                users.get("customer"), List.of());

        createReservation("Tran Hoang Nam", "0922222222", 4, ReservationStatus.CONFIRMED,
                LocalDateTime.now().plusHours(4), "Seed reservation - confirmed",
                users.get("customer"), List.of(tables.get("A02")));

        createReservation("Le Thu Ha", "0933333333", 3, ReservationStatus.CHECKED_IN,
                LocalDateTime.now().minusMinutes(45), "Seed reservation - checked in",
                users.get("waiter"), List.of(tables.get("A03")));

        createReservation("Pham Quoc Bao", "0944444444", 6, ReservationStatus.COMPLETED,
                LocalDateTime.now().minusHours(3), "Seed reservation - completed",
                users.get("waiter2"), List.of(tables.get("D01")));

        createReservation("Hoang Gia Linh", "0955555555", 8, ReservationStatus.CANCELLED,
                LocalDateTime.now().plusDays(1), "Seed reservation - cancelled",
                users.get("customer"), List.of(tables.get("C01")));
    }

    private void createReservation(String fullName, String phone, Integer guests, ReservationStatus status,
                                   LocalDateTime reservationTime, String note, User user,
                                   List<RestaurantTable> assignedTables) {
        Reservation reservation = Reservation.builder()
                .fullName(fullName)
                .phone(phone)
                .numberOfGuests(guests)
                .status(status)
                .reservationTime(reservationTime)
                .note(note)
                .user(user)
                .reservationTables(new HashSet<>())
                .build();

        for (RestaurantTable table : assignedTables) {
            if (table != null) {
                reservation.getReservationTables().add(ReservationTable.builder()
                        .reservation(reservation)
                        .table(table)
                        .build());
            }
        }

        reservationRepository.save(reservation);
    }

    private void seedOrders(Map<String, User> users, Map<String, RestaurantTable> tables,
                            Map<String, MenuItem> menuItems) {
        if (orderRepository.count() > 0) {
            return;
        }

        createOrder(users.get("waiter"), tables.get("A03"), OrderStatus.PREPARING,
                List.of(
                        orderItem(menuItems.get("spring rolls"), 2, OrderItemStatus.READY, "Less sauce"),
                        orderItem(menuItems.get("beef steak"), 1, OrderItemStatus.PREPARING, "Medium rare"),
                        orderItem(menuItems.get("iced tea"), 2, OrderItemStatus.SERVED, "")
                ));

        createOrder(users.get("waiter2"), tables.get("D01"), OrderStatus.SERVED,
                List.of(
                        orderItem(menuItems.get("seafood fried rice"), 2, OrderItemStatus.SERVED, ""),
                        orderItem(menuItems.get("orange juice"), 2, OrderItemStatus.SERVED, "")
                ));

        createOrder(users.get("waiter"), tables.get("B01"), OrderStatus.COMPLETED,
                List.of(
                        orderItem(menuItems.get("beef pho"), 2, OrderItemStatus.SERVED, ""),
                        orderItem(menuItems.get("tiramisu"), 1, OrderItemStatus.SERVED, "")
                ));
    }

    private OrderItem orderItem(MenuItem menuItem, Integer quantity, OrderItemStatus status, String note) {
        return OrderItem.builder()
                .menuItem(menuItem)
                .quantity(quantity)
                .unitPrice(menuItem.getPrice())
                .status(status)
                .specialNote(note)
                .build();
    }

    private void createOrder(User waiter, RestaurantTable table, OrderStatus status, List<OrderItem> orderItems) {
        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(waiter)
                .table(table)
                .status(status)
                .totalAmount(totalAmount)
                .orderItems(new ArrayList<>())
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getOrderItems().add(item);
        }

        orderRepository.save(order);
    }
}
