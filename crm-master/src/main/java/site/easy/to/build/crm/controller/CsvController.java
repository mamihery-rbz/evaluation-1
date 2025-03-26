package site.easy.to.build.crm.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.csv.*;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/csv")
public class CsvController {

    private final CsvService csvService;
    private final EntityScannerService entityScannerService;
    private final CsvImportCustomer csvImportCustomer;
    private final UserService userService;

    private final ImportationFichierService importationFichierService;
    private final AuthenticationUtils authenticationUtils;
    private final CustomerService customerService;

    private final CustomerLoginInfoService customerLoginInfoService;

    private final BudgetImportCustomer budgetImportCustomer;

    private final BudgetService budgetService;

    private final BudgetRepository budgetRepository;

    public CsvController(CsvService csvService, EntityScannerService entityScannerService, DataSource dataSource, CsvImportCustomer csvImportCustomer, UserService userService, ImportationFichierService importationFichierService, AuthenticationUtils authenticationUtils, CustomerService customerService, CustomerLoginInfoService customerLoginInfoService, BudgetImportCustomer budgetImportCustomer, BudgetService budgetService, BudgetRepository budgetRepository) {
        this.csvService = csvService;
        this.entityScannerService = entityScannerService;
        this.csvImportCustomer = csvImportCustomer;
        this.userService = userService;
        this.importationFichierService = importationFichierService;
        this.authenticationUtils = authenticationUtils;
        this.customerService = customerService;
        this.customerLoginInfoService = customerLoginInfoService;
        this.budgetImportCustomer = budgetImportCustomer;
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
    }
    @GetMapping("/form")
    public String showForm(Model model){
        Set<Class<?>> entities = entityScannerService.getAllEntities();
        char[] separators = {',', ';'};
        model.addAttribute("entities",entities);
        model.addAttribute("separators",separators);
        return "csv/import";
    }

    @PostMapping("/upload")
    public String uploadCsv(@RequestParam("file") MultipartFile file,@RequestParam("file1") MultipartFile file1, @RequestParam("file2") MultipartFile file2,RedirectAttributes redirectAttributes,
                            @RequestParam("separator")char separator,Model model, Authentication authentication) throws Exception{
        List<Customer> customerList= new ArrayList<>();
        List<Budget> budgets = new ArrayList<>();
        try {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            User user = userService.findById(userId);
            customerList = csvImportCustomer.importCustomerCsv(file,separator,user);

            budgets = budgetImportCustomer.importBudgetCsv(file2,separator,customerList);
            importationFichierService.importationFeuille1(file1,separator,customerList,budgets);

            model.addAttribute("message","Fichier CSV importé avec succès !");
            return "redirect:/csv/form";
        } catch (Exception e) {
            for (int i = 0; i < budgets.size(); i++) {
                budgetRepository.delete(budgets.get(i));
            }
            for (int i = 0; i < customerList.size(); i++) {

                CustomerLoginInfo customerLoginInfo = customerLoginInfoService.findByEmail(customerList.get(i).getEmail());
                customerService.delete(customerList.get(i));
                customerLoginInfoService.delete(customerLoginInfo);
            }
            e.printStackTrace();
            String errorMessage = buildDetailedErrorMessage(e);
            char[] separators = {',', ';'};
            model.addAttribute("separators",separators);
            model.addAttribute("messageError",errorMessage);
            return "csv/import";
        }
    }

    private Class<?> getEntityClass(String entityName) throws ClassNotFoundException {
        String packageName = "site.easy.to.build.crm.entity";
        return Class.forName(packageName + "." + entityName);
    }

    private String buildDetailedErrorMessage(Throwable e) {
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(e.getMessage());

        // Ajout des causes sous-jacentes
        Throwable cause = e.getCause();
        while (cause != null) {
            errorMessage.append("\nCause: ").append(cause.getMessage());
            cause = cause.getCause();
        }

        return errorMessage.toString();
    }

}
