package site.easy.to.build.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import site.easy.to.build.crm.service.data.GenerateService;

@Controller
@RequestMapping("/generate/data")
public class GenerateDataController {
    private final GenerateService generateService;

    public GenerateDataController(GenerateService generateService) {
        this.generateService = generateService;
    }

    @GetMapping("/generatePage")
    public String generatePage(Model model) {
        return "generate/generate";
    }

    @PostMapping("/generate")
    public String generateDate(@RequestParam String tableName,
                               @RequestParam int minRows,
                               @RequestParam int maxRows,
                               Model model) {
        try {
            generateService.generateRandomData(tableName, minRows, maxRows);
            model.addAttribute("message", "Données générées avec succès pour la table " + tableName);
        }catch ( Exception e) {
            model.addAttribute("message", "Erreur : " + e.getMessage());
        }
        return "generate/generate";
    }
}