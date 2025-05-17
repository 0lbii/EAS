package services;

import models.GamePlayer;
import org.springframework.stereotype.Service;
import edu.asu.stratego.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

@Service
public class GamePlayerService {

    public void saveGamePlayer(GamePlayer gamePlayer) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (gamePlayer.getId() == null) {
                em.persist(gamePlayer);
            } else {
                em.merge(gamePlayer);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive())
                tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}