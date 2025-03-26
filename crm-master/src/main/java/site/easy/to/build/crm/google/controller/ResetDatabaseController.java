package site.easy.to.build.crm.google.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import site.easy.to.build.crm.service.evaluation.ConfigurationBaseService;

@Controller
public class ResetDatabaseController {
    private final ConfigurationBaseService configurationBaseService;

    public ResetDatabaseController(ConfigurationBaseService configurationBaseService){
        this.configurationBaseService = configurationBaseService;
    }

    @RequestMapping("/resetDatabase")
    public String resetDatabase(){
        configurationBaseService.truncateAllTables();
        return "redirect:/";
    }
}
