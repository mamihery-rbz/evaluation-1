package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Depense;
import site.easy.to.build.crm.entity.Notification;
import site.easy.to.build.crm.repository.DepenseRepository;
import site.easy.to.build.crm.service.depense.DepenseService;
import site.easy.to.build.crm.service.notification.NotificationService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthenticationUtils authenticationUtils;
    private final DepenseService depenseService;

    private final DepenseRepository depenseRepository;
    @Autowired
    public NotificationController(NotificationService notificationService, AuthenticationUtils authenticationUtils, DepenseService depenseService, DepenseRepository depenseRepository) {
        this.notificationService = notificationService;
        this.authenticationUtils = authenticationUtils;
        this.depenseService = depenseService;
        this.depenseRepository = depenseRepository;
    }



    @GetMapping("/notif")
    public String getNotificationsByUser(Authentication authentication, Model model) {
        // Récupérer l'ID de l'utilisateur connecté
        int userId = authenticationUtils.getLoggedInUserId(authentication);

        // Récupérer les notifications de cet utilisateur
        List<Notification> notifications = notificationService.getRecentNotifications(userId);
        model.addAttribute("notifications", notifications);

        return "notification/notificationView";
    }

    // 🔹 Mettre à jour l'état d'une notification
    @PostMapping("/{notificationId}/etat/{newEtat}")
    public String updateNotificationEtat(@PathVariable int notificationId, @PathVariable int newEtat, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);

        // Mettre à jour l'état de la notification
        notificationService.updateNotificationEtat(notificationId, newEtat);
        Notification notification = notificationService.findByNotificationId(notificationId);
        Depense depense = depenseRepository.findById(notification.getIdDepense()).get();
        if(depense!= null){
            depense.setEtat(newEtat);
            depenseService.save(depense);
        }


        return "redirect:/notifications/notif";
    }
}
