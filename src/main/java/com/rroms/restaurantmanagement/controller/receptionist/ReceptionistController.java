package com.rroms.restaurantmanagement.controller.receptionist;

import com.rroms.restaurantmanagement.service.TableService;
import org.springframework.stereotype.Controller;
import lombok.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/receptionist")
public class ReceptionistController {
    private final TableService tableService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "receptionist/home";
    }

    @GetMapping("/tables")
    public String listTable(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String capacity,
            Model model
    ){
        model.addAttribute("tables",  tableService.filterTables(keyword, area, capacity));

        model.addAttribute("keyword",keyword);
        model.addAttribute("area",area);
        model.addAttribute("capacity",capacity);
        model.addAttribute("pageTitle", "Tables");
        model.addAttribute("currentPage", "tables");
        return "receptionist/roomlist";
    }
}
