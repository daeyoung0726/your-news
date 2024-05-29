package project.yourNews.domains.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.yourNews.domains.member.domain.Member;
import project.yourNews.domains.member.dto.MemberInfoDto;
import project.yourNews.domains.member.repository.MemberRepository;
import project.yourNews.handler.exceptionHandler.error.ErrorCode;
import project.yourNews.handler.exceptionHandler.exception.CustomException;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminService {

    private final MemberRepository memberRepository;

    /* 사용자 전체 불러오기 */
    @Transactional(readOnly = true)
    public Page<MemberInfoDto> findAllMembers(Pageable pageable) {

        Page<Member> members = memberRepository.findAll(pageable);

        return members.map(MemberInfoDto::new);
    }

    /* 사용자 탈퇴 */
    @Transactional
    public void dropMember(Long memberId) {

        Member findMember = memberRepository.findById(memberId).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        memberRepository.delete(findMember);
    }
}
