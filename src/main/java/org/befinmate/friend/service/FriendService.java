package org.befinmate.friend.service;

import org.befinmate.dto.request.FriendRequest;
import org.befinmate.dto.response.FriendResponse;

import java.util.List;

public interface FriendService {

    void sendFriendRequest(String userId, FriendRequest request);

    List<FriendResponse> getFriends(String userId);

    List<FriendResponse> getIncomingRequests(String userId);

    List<FriendResponse> getOutgoingRequests(String userId);

    void acceptRequest(String userId, String friendshipId);

    void rejectRequest(String userId, String friendshipId);

    void removeFriend(String userId, String friendshipId);
}
