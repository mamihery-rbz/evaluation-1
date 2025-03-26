package site.easy.to.build.crm.service.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.util.EmailTokenUtils;

import jakarta.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CsvImportCustomer {

    private final CustomerLoginInfoService customerLoginInfoService;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CsvImportCustomer(CustomerLoginInfoService customerLoginInfoService,
                             CustomerService customerService,
                             PasswordEncoder passwordEncoder) {
        this.customerLoginInfoService = customerLoginInfoService;
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public List<Customer> importCustomerCsv(MultipartFile file, char separator, User user) throws Exception {
        List<Customer> customerList = new ArrayList<>();
        List<CustomerLoginInfo> customerLoginInfos = new ArrayList<>();
        Map<String, String> emailNameMap = new HashMap<>();
        String rawPassword = "12345678";
        String fileName = file.getOriginalFilename();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withDelimiter(separator)
                     .withFirstRecordAsHeader()
                     .withIgnoreEmptyLines(true)
                     .withTrim())) {

            if (!csvParser.getHeaderMap().containsKey("customer_email") ||
                    !csvParser.getHeaderMap().containsKey("customer_name")) {
                throw new Exception("Le fichier CSV '"+fileName+"' ne contient pas les en-têtes requis (customer_email et customer_name).");
            }

            for (CSVRecord record : csvParser) {
                try {
                    String email = record.get("customer_email").trim();
                    String name = record.get("customer_name").trim();

                    if (email.isEmpty() || name.isEmpty()) {
                        throw new Exception("Ligne ignorée : Email ou Nom vide. Fichier : "+fileName+ "Ligne " + record.getRecordNumber());
                    }

                    String token = EmailTokenUtils.generateToken();

                    CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
                    customerLoginInfo.setEmail(email);
                    customerLoginInfo.setPassword(passwordEncoder.encode(rawPassword));
                    customerLoginInfo.setToken(token);
                    customerLoginInfo.setPasswordSet(true);

                    customerLoginInfos.add(customerLoginInfo);
                    emailNameMap.put(email, name);
                } catch (Exception e) {
                    throw new Exception("Erreur lors de l'analyse de la ligne " + record.getRecordNumber() + ": "+"Fichier : "+fileName+" : "+  e.getMessage());
                }
            }

            for (CustomerLoginInfo customerLoginInfo : customerLoginInfos) {
                try {
                    customerLoginInfoService.save(customerLoginInfo);
                } catch (Exception e) {
                    throw new Exception("Erreur lors de la sauvegarde de CustomerLoginInfo : " + customerLoginInfo.getEmail(), e);
                }
            }

            for (Map.Entry<String, String> entry : emailNameMap.entrySet()) {
                try {
                    String email = entry.getKey();
                    String name = entry.getValue();

                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setEmail(email);
                    customer.setUser(user);
                    customer.setCountry("Madagascar");
                    customer.setCreatedAt(LocalDateTime.now());

                    customerList.add(customer);
                } catch (Exception e) {
                    throw new Exception("Erreur lors de la création du client avec l'email " + entry.getKey() + ": " + e.getMessage());
                }
            }

            for (Customer customer : customerList) {
                try {
                    customerService.save(customer);
                } catch (Exception e) {
                    throw new Exception("Erreur lors de la sauvegarde du Customer : " + customer.getEmail(), e);
                }
            }

        } catch (IOException e) {
            throw new Exception("Erreur de lecture du fichier CSV '"+fileName+"'.", e);
        } catch (Exception exception) {
            throw new Exception("Erreur générale lors de l'importation du fichier CSV '"+fileName+"'", exception);
        }

        return customerList;
    }


}
