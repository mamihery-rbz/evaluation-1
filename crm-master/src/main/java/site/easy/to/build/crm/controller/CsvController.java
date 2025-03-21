package site.easy.to.build.crm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import site.easy.to.build.crm.service.csv.CsvService;
import site.easy.to.build.crm.service.csv.EntityScannerService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

@Controller
@RequestMapping("/csv")
public class CsvController {

    private final CsvService csvService;
    private final EntityScannerService entityScannerService;
    private final DataSource dataSource;

    public CsvController(CsvService csvService, EntityScannerService entityScannerService, DataSource dataSource) {
        this.csvService = csvService;
        this.entityScannerService = entityScannerService;
        this.dataSource = dataSource;
    }
    @GetMapping("/form")
    public String showForm(Model model){
        Set<Class<?>> entities = entityScannerService.getAllEntities();
        model.addAttribute("entities",entities);
        return "csv/import";
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file,
                                            @RequestParam("entityName") String entityName) throws Exception{
        Connection connection=null;
        try{
            connection=dataSource.getConnection();
        }
        catch(SQLException e){
            return ResponseEntity.internalServerError().body("Erreur lors de la connection a la base : " + e.getMessage());
        }
        try {
            Class<?> entityClass = getEntityClass(entityName);
            csvService.insertDataFromCsv(file, entityClass,connection);
            return ResponseEntity.ok("Fichier CSV importé avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Erreur lors de l'importation : " + e.getMessage());
        }
        finally {
            if (connection!=null){
                try{
                    connection.close();
                }
                catch(SQLException e){
                    return ResponseEntity.internalServerError().body("Erreur lors de la fermeture de la connection : " + e.getMessage());
                }
            }
        }
    }

    private Class<?> getEntityClass(String entityName) throws ClassNotFoundException {
        String packageName = "site.easy.to.build.crm.entity";
        return Class.forName(packageName + "." + entityName);
    }
}
