package org.befinmate.friend.implement;

import lombok.RequiredArgsConstructor;
import org.befinmate.auth.repository.UserRepository;
import org.befinmate.common.enums.FriendStatus;
import org.befinmate.dto.request.FriendRequest;
import org.befinmate.dto.response.FriendResponse;
import org.befinmate.entity.Friendship;
import org.befinmate.entity.User;
import org.befinmate.friend.repository.FriendRepository;
import org.befinmate.friend.service.FriendService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public void sendFriendRequest(String userId, FriendRequest request) {
        User fromUser = getUserOrThrow(userId);
        User toUser = userRepository.findByEmail(request.getTargetEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

        if (fromUser.getId().equals(toUser.getId())) {
            throw new IllegalArgumentException("Cannot add yourself as friend");
        }

        friendRepository.findByUserPair(fromUser.getId(), toUser.getId())
                .ifPresent(f -> {
                    throw new IllegalStateException("Friendship or request already exists");
                });

        Friendship friendship = Friendship.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .status(FriendStatus.PENDING)
                .build();

        friendRepository.save(friendship);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(String userId) {
        User current = getUserOrThrow(userId);

        return friendRepository.findAllForUser(current.getId())
                .stream()
                .filter(f -> f.getStatus() == FriendStatus.ACCEPTED && !f.isDeleted())
                .map(f -> toResponse(current.getId(), f))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getIncomingRequests(String userId) {
        User current = getUserOrThrow(userId);

        return friendRepository.findByToUser_IdAndStatusAndDeletedFalse(
                        current.getId(), FriendStatus.PENDING)
                .stream()
                .map(f -> toResponse(current.getId(), f))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendResponse> getOutgoingRequests(String userId) {
        User current = getUserOrThrow(userId);

        return friendRepository.findByFromUser_IdAndStatusAndDeletedFalse(
                        current.getId(), FriendStatus.PENDING)
                .stream()
                .map(f -> toResponse(current.getId(), f))
                .toList();
    }

    @Override
    public void acceptRequest(String userId, String friendshipId) {
        Friendship friendship = getFriendshipForAction(userId, friendshipId);

        if (!friendship.getToUser().getId().equals(userId)) {
            throw new IllegalStateException("Only receiver can accept request");
        }

        friendship.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(friendship);
    }

    @Override
    public void rejectRequest(String userId, String friendshipId) {
        Friendship friendship = getFriendshipForAction(userId, friendshipId);

        if (!friendship.getToUser().getId().equals(userId)) {
            throw new IllegalStateException("Only receiver can reject request");
        }

        friendship.setStatus(FriendStatus.REJECTED);
        friendRepository.save(friendship);
    }

    @Override
    public void removeFriend(String userId, String friendshipId) {
        Friendship friendship = getFriendshipForAction(userId, friendshipId);

        if (friendship.getStatus() != FriendStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot remove non-accepted friendship");
        }

        // soft delete theo style BaseEntity
        friendship.setDeleted(true);
        friendRepository.save(friendship);
    }

    private Friendship getFriendshipForAction(String userId, String friendshipId) {
        Friendship friendship = friendRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

        if (friendship.isDeleted()) {
            throw new IllegalStateException("Friendship already deleted");
        }

        if (!friendship.getFromUser().getId().equals(userId)
                && !friendship.getToUser().getId().equals(userId)) {
            throw new IllegalStateException("You are not part of this friendship");
        }
        return friendship;
    }

    private FriendResponse toResponse(String currentUserId, Friendship f) {
        boolean incoming = f.getToUser().getId().equals(currentUserId);
        User other = incoming ? f.getFromUser() : f.getToUser();

        return FriendResponse.builder()
                .id(f.getId())
                .friendUserId(other.getId())
                .friendName(other.getFullName())
                .friendEmail(other.getEmail())
                .status(f.getStatus())
                .incoming(incoming)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
