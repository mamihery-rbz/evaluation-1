package site.easy.to.build.crm.service.csv;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class EntityScannerService {

    private final EntityManager entityManager;

    public EntityScannerService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Set<Class<?>> getAllEntities() {
        Set<Class<?>> entityClasses = new HashSet<>();
        System.out.println("goooo");

        // Récupère le modèle de métadonnées JPA
        Metamodel metamodel = entityManager.getMetamodel();

        // Récupère toutes les entités dans le modèle de métadonnées
        for (EntityType<?> entityType : metamodel.getEntities()) {
            entityClasses.add(entityType.getJavaType());
        }

        return entityClasses;
    }
}

