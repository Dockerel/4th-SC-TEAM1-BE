package com.gdg.Todak.tree.business;

import com.gdg.Todak.common.exception.TodakException;
import com.gdg.Todak.friend.service.FriendCheckService;
import com.gdg.Todak.member.domain.Member;
import com.gdg.Todak.member.repository.MemberRepository;
import com.gdg.Todak.point.facade.PointFacade;
import com.gdg.Todak.tree.business.dto.TreeInfoResponse;
import com.gdg.Todak.tree.domain.GrowthButton;
import com.gdg.Todak.tree.domain.Tree;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.gdg.Todak.common.exception.errors.PointError.USER_NOT_FOUND_ERROR;
import static com.gdg.Todak.common.exception.errors.TreeError.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TreeService {

    private final TreeRepository treeRepository;
    private final MemberRepository memberRepository;
    private final PointFacade pointFacade;
    private final FriendCheckService friendCheckService;

    @Transactional
    public void getTree(Member member) {
        if (treeRepository.existsByMember(member)) {
            throw new TodakException(TREE_LIMIT_ERROR);
        }

        treeRepository.saveTreeByMember(member);
    }

    @Transactional
    public String earnExperience(String userId, GrowthButton growthButton) {
        Member member = getMember(userId);

        Tree tree = treeRepository.findByMember(member).toDomain();

        if (tree.isMaxGrowth()) {
            throw new TodakException(MAX_LEVEL_ERROR);
        }

        pointFacade.consumePointByGrowthButton(member, growthButton);
        tree.earnExperience(growthButton);

        treeRepository.update(member, tree.toTreeEntityUpdateRequest());

        return "정상적으로 경험치를 획득하였습니다.";
    }

    public TreeInfoResponse getMyTreeInfo(String userId) {
        Member member = getMember(userId);

        Tree tree = treeRepository.findByMember(member).toDomain();

        if (!tree.isMyTree(member)) {
            throw new TodakException(ONLY_OWN_TREE_LOOKUP_ERROR);
        }

        return tree.toTreeInfoResponse();
    }

    public TreeInfoResponse getFriendTreeInfo(String userId, String friendId) {
        Member friendMember = getMember(friendId);

        List<Member> acceptedMembers = friendCheckService.getFriendMembers(userId);

        if (!acceptedMembers.contains(friendMember)) {
            throw new TodakException(ONLY_FRIEND_TREE_LOOKUP_ERROR);
        }

        Tree tree = treeRepository.findByMember(friendMember).toDomain();

        return tree.toTreeInfoResponse();
    }

    private Member getMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new TodakException(USER_NOT_FOUND_ERROR));
    }
}
