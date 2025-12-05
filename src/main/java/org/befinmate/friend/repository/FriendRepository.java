package org.befinmate.friend.repository;

import org.befinmate.common.enums.FriendStatus;
import org.befinmate.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friendship, String> {

    @Query("""
           SELECT f
           FROM Friendship f
           WHERE ((f.fromUser.id = :user1 AND f.toUser.id = :user2)
              OR (f.fromUser.id = :user2 AND f.toUser.id = :user1))
             AND f.deleted = false
           """)
    Optional<Friendship> findByUserPair(@Param("user1") String user1,
                                        @Param("user2") String user2);

    @Query("""
           SELECT f
           FROM Friendship f
           WHERE (f.fromUser.id = :userId OR f.toUser.id = :userId)
             AND f.deleted = false
           """)
    List<Friendship> findAllForUser(@Param("userId") String userId);

    List<Friendship> findByToUser_IdAndStatusAndDeletedFalse(String userId, FriendStatus status);

    List<Friendship> findByFromUser_IdAndStatusAndDeletedFalse(String userId, FriendStatus status);
}
