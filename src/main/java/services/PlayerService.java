package services;

import models.Player;

import org.springframework.stereotype.Service;

import edu.asu.stratego.util.JpaUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

@Service
public class PlayerService {

    public void savePlayer(Player player) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (player.getId() == null) {
                em.persist(player);
            } else {
                em.merge(player);
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

    public Player findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Player.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    public Player findByNickname(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Player p WHERE p.nickname = :name", Player.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Player findByEmail(String email) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Player p WHERE p.email = :email", Player.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Player findByEmailAndPassword(String email, String password) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Player p WHERE p.email = :email AND p.password = :password",
                    Player.class)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
