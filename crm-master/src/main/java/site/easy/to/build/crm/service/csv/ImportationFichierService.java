package site.easy.to.build.crm.service.csv;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.notification.NotificationService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
@Service
public class ImportationFichierService {
    private final CustomerService customerService;
    private final LeadService leadService;
    private final TicketService ticketService;
    private final UserService userService;
    private final DepenseService depenseService;
    private final BudgetService budgetService;
    private final NotificationService notifService;

    public ImportationFichierService(CustomerService customerService, LeadService leadService, TicketService ticketService, UserService userService, DepenseService depenseService, BudgetService budgetService, NotificationService notifService) {
        this.customerService = customerService;
        this.leadService = leadService;
        this.ticketService = ticketService;
        this.userService = userService;
        this.depenseService = depenseService;
        this.budgetService = budgetService;
        this.notifService = notifService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void importationFeuille1(MultipartFile file, char separator, List<Customer> customerList,List<Budget> budgets)throws Exception{
        String fileName = file.getOriginalFilename();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             @SuppressWarnings("deprecation")
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withDelimiter(separator))) {
            for (CSVRecord csvRecord : csvParser) {
                String customer_email = csvRecord.get("customer_email").trim();
                if(customer_email.isEmpty()) throw new Exception("email inexistant a la ligne "+csvRecord.getRecordNumber());
//                Customer customer = customerService.findByEmail(customer_email);
//                if(customer == null) throw new Exception("customer inexistant");
                Customer customer = null;
                boolean verification = false;
                for (int i = 0; i < customerList.size(); i++) {
                    if(customerList.get(i).getEmail().equalsIgnoreCase(customer_email)){
                        verification = true;
                        customer = customerList.get(i);
                        break;
                    }
                }
                if(!verification) throw new Exception("Customer inexistant à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");
                if(customer == null) throw new Exception("Customer inexistant pour l'email à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");
                String expense = csvRecord.get("expense").trim();
                if(expense.isEmpty()) throw new Exception("Expense inexistant à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");
                int indexVirgule = expense.indexOf(',');
                if(indexVirgule!=-1){
                    StringBuilder sb = new StringBuilder(expense);
                    sb.setCharAt(indexVirgule, '.');
                    expense = sb.toString();
                }


                double depense = Double.parseDouble(expense);
                if(depense<0){
                    throw new Exception("Montant négatif à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");
                }
                String subject_or_name = csvRecord.get("subject_or_name").trim();
                if(subject_or_name.isEmpty()) throw new Exception("Subject_or_name inexistant à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");

                String status = csvRecord.get("status").trim();
                if(status.isEmpty()) throw new Exception("Status inexistant à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");
                String type = csvRecord.get("type").trim();
                if(type.isEmpty()) throw new Exception("Type inexistant à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");
                List<User> manager = userService.getUsersByRoleId(1);
                System.out.println("manager : "+manager.size());
                List<User> employe = userService.getUsersByRoleId(2);
                System.out.println("employe : "+employe.size());

                int maxUser = 2;
                int minUser = 1;
                Random random = new Random();
                int randomUser = random.nextInt(maxUser - minUser + 1) + minUser;
                if(status == null) throw new Exception("Status invalid a la ligne "+csvRecord.getRecordNumber());
                else{
                    if(type.equalsIgnoreCase("lead")){
                        if(!(status.equalsIgnoreCase("assign-to-sales") || status.equalsIgnoreCase("success") ||status.equalsIgnoreCase("archived") || status.equalsIgnoreCase("scheduled")  || status.equalsIgnoreCase("meeting-to-schedule") )){
                            throw new Exception("Status invalid à la ligne "+csvRecord.getRecordNumber()+" dans le fichier '"+fileName+"'");
                        }else{
                            int min = 0;
                            Lead lead= new Lead();
                            int maxManager = manager.size()-1;
                            System.out.println("Max manager : "+ maxManager);
                            if (maxManager >= min) {
                                System.out.println("Max manager : "+ maxManager + "superieur a "+min);
                            }
                            int randomManager = random.nextInt(maxManager - min + 1) + min;
                            User managerLead = manager.get(randomManager);
                            if(randomUser ==1){
                                lead.setName(subject_or_name);
                                lead.setCustomer(customer);
                                lead.setManager(managerLead);
                                lead.setPhone("0300000000");
                                lead.setEmployee(managerLead);
                                lead.setStatus(status);
                                lead.setGoogleDrive(false);
                                lead.setCreatedAt(LocalDateTime.now());
                                lead= leadService.save(lead);
                                Notification notif = budgetService.verifyBudget(customer.getCustomerId(), depense);
                                Depense depense1 = new Depense();
                                depense1.setMontant(BigDecimal.valueOf(depense));
                                depense1.setEtat(notif.getEtat());
                                depense1.setLead(lead);
                                depense1.setCreated_at(LocalDateTime.now());
                                depense1 =  depenseService.save(depense1);
                                notif.setEtat(0);
                                notif.setIdDepense(depense1.getId().intValue());
                                System.out.println("notif.getMessage()" + notif.getMessage());
                                if (!notif.getMessage().equals("successful")) {
                                    notifService.save(notif);
                                }
                            }else{
                                int max = employe.size()-1;
                                System.out.println("Max manager : "+ max + "superieur a "+min);
                                lead.setName(subject_or_name);
                                lead.setCustomer(customer);
                                int randomEmploye = random.nextInt(max - min + 1) + min;
                                User employeLead = employe.get(randomEmploye);
                                lead.setManager(managerLead);
                                lead.setPhone("0300000000");
                                lead.setEmployee(employeLead);
                                lead.setStatus(status);
                                lead.setGoogleDrive(false);
                                lead.setCreatedAt(LocalDateTime.now());
                                lead = leadService.save(lead);
                                Notification notif = budgetService.verifyBudget(customer.getCustomerId(), depense);
                                Depense depense1 = new Depense();
                                depense1.setMontant(BigDecimal.valueOf(depense));
                                depense1.setEtat(notif.getEtat());
                                depense1.setLead(lead);
                                depense1.setCreated_at(LocalDateTime.now());
                                depense1= depenseService.save(depense1);
                                notif.setEtat(0);
                                notif.setIdDepense(depense1.getId().intValue());
                                System.out.println("notif.getMessage()" + notif.getMessage());
                                if (!notif.getMessage().equals("successful")) {
                                    notifService.save(notif);
                                }
                            }
                        }
                    } else if (type.equalsIgnoreCase("ticket")) {
                        if(!( status.equalsIgnoreCase("archived") || status.equalsIgnoreCase("escalated") ||status.equalsIgnoreCase("pending-customer-response") ||status.equalsIgnoreCase("reopened") ||status.equalsIgnoreCase("closed") || status.equalsIgnoreCase("open") || status.equalsIgnoreCase("assigned") ||status.equalsIgnoreCase("on-hold") || status.equalsIgnoreCase("in-progress")  || status.equalsIgnoreCase("resolved") )){
                            throw new Exception("Status invalid a la ligne "+csvRecord.getRecordNumber());
                        }else{
                            int min = 0;
                            Ticket ticket = new Ticket();
                            int maxManager =manager.size()-1;
                            int randomManager = random.nextInt(maxManager - min + 1) + min;
                            User managerLead = manager.get(randomManager);
                            if(randomUser == 1){
                                ticket.setSubject(subject_or_name);
                                ticket.setDescription("csv");
                                ticket.setStatus(status);
                                ticket.setPriority("low");
                                ticket.setCustomer(customer);
                                ticket.setManager(managerLead);
                                ticket.setEmployee(managerLead);
                                ticket.setCreatedAt(LocalDateTime.now());
                                ticket = ticketService.save(ticket);
                                Notification notif = budgetService.verifyBudget(customer.getCustomerId(), depense);
                                Depense depense1 = new Depense();
                                depense1.setMontant(BigDecimal.valueOf(depense));
                                depense1.setEtat(notif.getEtat());
                                depense1.setTicket(ticket);
                                depense1.setCreated_at(LocalDateTime.now());
                                depense1= depenseService.save(depense1);
                                notif.setEtat(0);
                                notif.setIdDepense(depense1.getId().intValue());
                                System.out.println("notif.getMessage()" + notif.getMessage());
                                if (!notif.getMessage().equals("successful")) {
                                    notifService.save(notif);
                                }

                            }else{
                                int max = employe.size()-1;
                                int randomEmploye = random.nextInt(max - min + 1) + min;
                                User employeLead = employe.get(randomEmploye);
                                ticket.setSubject(subject_or_name);
                                ticket.setDescription("csv");
                                ticket.setStatus(status);
                                ticket.setPriority("low");
                                ticket.setCustomer(customer);
                                ticket.setManager(managerLead);
                                ticket.setEmployee(employeLead);
                                ticket.setCreatedAt(LocalDateTime.now());
                                ticket = ticketService.save(ticket);
                                Notification notif = budgetService.verifyBudget(customer.getCustomerId(), depense);
                                Depense depense1 = new Depense();
                                depense1.setMontant(BigDecimal.valueOf(depense));
                                depense1.setEtat(notif.getEtat());
                                depense1.setTicket(ticket);
                                depense1.setCreated_at(LocalDateTime.now());
                                depense1= depenseService.save(depense1);
                                notif.setEtat(0);
                                notif.setIdDepense(depense1.getId().intValue());
                                System.out.println("notif.getMessage()" + notif.getMessage());
                                if (!notif.getMessage().equals("successful")) {
                                    notifService.save(notif);
                                }
                            }
                        }
                    }else{
                        throw new Exception("Type invalid a la ligne "+csvRecord.getRecordNumber());
                    }
                }

            }
        }catch (Exception e){
            throw e;
        }
    }
}
