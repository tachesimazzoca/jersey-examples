package app.core;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPA {
    public static final EntityManagerFactory ef =
            Persistence.createEntityManagerFactory("default");
    
    public static EntityManagerFactory ef() {
        return ef;
    }
}
