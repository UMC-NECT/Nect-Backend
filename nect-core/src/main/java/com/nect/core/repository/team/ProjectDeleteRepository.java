package com.nect.core.repository.team;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectDeleteRepository {

    @PersistenceContext
    private EntityManager em;



    public List<Long> findProcessIdsByProjectId(Long projectId) {
        return em.createQuery(
                        "SELECT p.id FROM Process p WHERE p.project.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getResultList();
    }

    public void deleteProcessMentions(List<Long> processIds) {
        if (processIds.isEmpty()) return;
        em.createQuery("DELETE FROM ProcessMention m WHERE m.process.id IN :processIds")
                .setParameter("processIds", processIds)
                .executeUpdate();
    }

    public void deleteProcessSharedDocuments(List<Long> processIds) {
        if (processIds.isEmpty()) return;
        em.createQuery("DELETE FROM ProcessSharedDocument psd WHERE psd.process.id IN :processIds")
                .setParameter("processIds", processIds)
                .executeUpdate();
    }

    public void deleteProcessUsers(List<Long> processIds) {
        if (processIds.isEmpty()) return;
        em.createQuery("DELETE FROM ProcessUser pu WHERE pu.process.id IN :processIds")
                .setParameter("processIds", processIds)
                .executeUpdate();
    }

    public void deleteProcessFields(List<Long> processIds) {
        if (processIds.isEmpty()) return;
        em.createQuery("DELETE FROM ProcessField pf WHERE pf.process.id IN :processIds")
                .setParameter("processIds", processIds)
                .executeUpdate();
    }

    public void deleteProcessFeedbacks(List<Long> processIds) {
        if (processIds.isEmpty()) return;
        em.createQuery("DELETE FROM ProcessFeedback pf WHERE pf.process.id IN :processIds")
                .setParameter("processIds", processIds)
                .executeUpdate();
    }

    public void deleteProcessTaskItems(List<Long> processIds) {
        if (processIds.isEmpty()) return;
        em.createQuery("DELETE FROM ProcessTaskItem ti WHERE ti.process.id IN :processIds")
                .setParameter("processIds", processIds)
                .executeUpdate();
    }

    public void deleteProcesses(Long projectId) {
        em.createQuery("DELETE FROM Process p WHERE p.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteSharedDocuments(Long projectId) {
        em.createQuery("DELETE FROM SharedDocument sd WHERE sd.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deletePosts(Long projectId) {
        em.createQuery("DELETE FROM Post p WHERE p.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }


    public List<Long> findChatRoomIdsByProjectId(Long projectId) {
        return em.createQuery(
                        "SELECT c.id FROM ChatRoom c WHERE c.project.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getResultList();
    }

    public void deleteChatFiles(List<Long> chatRoomIds) {
        if (chatRoomIds.isEmpty()) return;
        em.createQuery("DELETE FROM ChatFile cf WHERE cf.chatMessage.chatRoom.id IN :chatRoomIds")
                .setParameter("chatRoomIds", chatRoomIds)
                .executeUpdate();
    }

    public void deleteChatMessages(List<Long> chatRoomIds) {
        if (chatRoomIds.isEmpty()) return;
        em.createQuery("DELETE FROM ChatMessage m WHERE m.chatRoom.id IN :chatRoomIds")
                .setParameter("chatRoomIds", chatRoomIds)
                .executeUpdate();
    }

    public void deleteChatRoomUsers(List<Long> chatRoomIds) {
        if (chatRoomIds.isEmpty()) return;
        em.createQuery("DELETE FROM ChatRoomUser u WHERE u.chatRoom.id IN :chatRoomIds")
                .setParameter("chatRoomIds", chatRoomIds)
                .executeUpdate();
    }

    public void deleteChatRooms(Long projectId) {
        em.createQuery("DELETE FROM ChatRoom c WHERE c.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProjectSchedules(Long projectId) {
        em.createQuery("DELETE FROM ProjectSchedule s WHERE s.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProjectTeamRoles(Long projectId) {
        em.createQuery("DELETE FROM ProjectTeamRole r WHERE r.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProjectPlanFiles(Long projectId) {
        em.createQuery("DELETE FROM ProjectPlanFile f WHERE f.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProjectUserWorkDaily(Long projectId) {
        em.createQuery("DELETE FROM ProjectUserWorkDaily w WHERE w.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProjectHistory(Long projectId) {
        em.createQuery("DELETE FROM ProjectHistory h WHERE h.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteNotifications(Long projectId) {
        em.createQuery("DELETE FROM Notification n WHERE n.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteMatchings(Long projectId) {
        em.createQuery("DELETE FROM Matching m WHERE m.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteRecruitments(Long projectId) {
        em.createQuery("DELETE FROM Recruitment r WHERE r.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProjectInterests(Long projectId) {
        em.createQuery("DELETE FROM ProjectInterest i WHERE i.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteUserTeamRoles(Long projectId) {
        em.createQuery("DELETE FROM UserTeamRole r WHERE r.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProjectUsers(Long projectId) {
        em.createQuery("DELETE FROM ProjectUser u WHERE u.project.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }

    public void deleteProject(Long projectId) {
        em.createQuery("DELETE FROM Project p WHERE p.id = :projectId")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }
    public void flushAndClear() {
        em.flush();
        em.clear();
    }

    public void deleteRecruitmentRequirements(Long projectId) {
        em.createQuery(
                        "DELETE FROM RecruitmentRequirement rr WHERE rr.recruitment.id IN " +
                                "(SELECT r.id FROM Recruitment r WHERE r.project.id = :projectId)")
                .setParameter("projectId", projectId)
                .executeUpdate();
    }
}