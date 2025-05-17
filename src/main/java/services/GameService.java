package services;

import models.Game;
import models.Player;
import org.springframework.stereotype.Service;
import edu.asu.stratego.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

@Service
public class GameService {

    public void saveGame(Game game) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (game.getId() == null) {
                em.persist(game);
            } else {
                em.merge(game);
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

    public List<Game> findGamesByPlayerNickname(String nickname) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT g FROM Game g " +
                            "JOIN g.gamePlayers gp " +
                            "JOIN gp.player p " +
                            "WHERE p.nickname = :nickname " +
                            "ORDER BY g.endTime DESC",
                    Game.class)
                    .setParameter("nickname", nickname)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public String getGameResultForPlayer(Game game, String nickname) {
        if (game.getWinner() == null) {
            return "Finalizada";
        }

        EntityManager em = JpaUtil.getEntityManager();
        try {
            Player player = em.createQuery(
                    "SELECT p FROM Player p WHERE p.nickname = :nickname", Player.class)
                    .setParameter("nickname", nickname)
                    .getSingleResult();

            return game.getWinner().getId().equals(player.getId()) ? "Ganada" : "Perdida";
        } finally {
            em.close();
        }
    }
}