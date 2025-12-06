package org.befinmate.friend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.befinmate.dto.request.FriendRequest;
import org.befinmate.dto.response.FriendResponse;
import org.befinmate.friend.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    private String getUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    @PostMapping("/requests")
    public ResponseEntity<Void> sendFriendRequest(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FriendRequest request
    ) {
        friendService.sendFriendRequest(getUserId(jwt), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<FriendResponse> friends = friendService.getFriends(getUserId(jwt));
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendResponse>> getIncoming(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<FriendResponse> list = friendService.getIncomingRequests(getUserId(jwt));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendResponse>> getOutgoing(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<FriendResponse> list = friendService.getOutgoingRequests(getUserId(jwt));
        return ResponseEntity.ok(list);
    }

    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<Void> acceptRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String friendshipId
    ) {
        friendService.acceptRequest(getUserId(jwt), friendshipId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<Void> rejectRequest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String friendshipId
    ) {
        friendService.rejectRequest(getUserId(jwt), friendshipId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFriend(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") String friendshipId
    ) {
        friendService.removeFriend(getUserId(jwt), friendshipId);
        return ResponseEntity.ok().build();
    }
}
