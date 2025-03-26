CREATE TABLE budget (
                        id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                        customer_id INT UNSIGNED NOT NULL,
                        montant DECIMAL(10,2) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);


CREATE TABLE depense (
                         id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                         ticket_id INT UNSIGNED,
                         lead_id INT UNSIGNED,
                         montant DECIMAL(10,2) NOT NULL,
                         etat int default 1,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (ticket_id) REFERENCES trigger_ticket(ticket_id) ON DELETE CASCADE ,
                         FOREIGN KEY (lead_id) REFERENCES trigger_lead(lead_id) ON DELETE CASCADE
);


CREATE TABLE taux_alert(
    taux_id INT AUTO_INCREMENT PRIMARY KEY,
    valeur DECIMAL(10, 2) NOT NULL ,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `notification` (
                                              `id` INT AUTO_INCREMENT PRIMARY KEY,
                                              `message` VARCHAR(255) NOT NULL,
    `customer_id` INT UNSIGNED NOT NULL,
    `date_notif` timestamp not null,
    CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`) ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS budget (
                                      id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                      customer_id INT UNSIGNED NOT NULL,
                                      montant DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table structure for table depense
CREATE TABLE IF NOT EXISTS depense (
                                       id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
                                       ticket_id INT UNSIGNED,
                                       lead_id INT UNSIGNED,
                                       montant DECIMAL(10,2) NOT NULL,
    etat INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES trigger_ticket(ticket_id) ON DELETE CASCADE,
    FOREIGN KEY (lead_id) REFERENCES trigger_lead(lead_id) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;




INSERT INTO taux_alert (taux_id, valeur, created_at) VALUES (1, 80, NOW());