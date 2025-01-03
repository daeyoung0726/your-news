package project.yourNews.domains.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.yourNews.common.exception.CustomException;
import project.yourNews.common.exception.error.ErrorCode;
import project.yourNews.common.mail.stibee.service.StibeeService;
import project.yourNews.common.utils.redis.RedisUtil;
import project.yourNews.domains.common.service.AssociatedEntityService;
import project.yourNews.domains.member.domain.Member;
import project.yourNews.domains.member.dto.MemberResponseDto;
import project.yourNews.domains.member.dto.MemberUpdateDto;
import project.yourNews.domains.member.dto.SignUpDto;
import project.yourNews.domains.member.dto.SubscribeUpdateDto;
import project.yourNews.domains.member.repository.MemberRepository;
import project.yourNews.domains.subNews.service.SubNewsService;

import java.util.List;

import static project.yourNews.common.utils.redis.RedisProperties.CODE_KEY_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubNewsService subNewsService;
    private final AssociatedEntityService associatedEntityService;
    private final StibeeService stibeeService;
    private final RedisUtil redisUtil;

    private static final String YU_NEWS_NAME = "영대소식";
    private static final String USERNAME_PATTERN = "^[ㄱ-ㅎ가-힣a-z0-9-_]{4,20}$";
    private static final String NICKNAME_PATTERN = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$";

    /* 회원가입 메서드 */
    @Transactional
    public void signUp(SignUpDto signUpDto) {

        String key = CODE_KEY_PREFIX + signUpDto.getEmail();

        if (!signUpDto.getVerificationCode().equals(redisUtil.get(key))) {  // api 플랫폼 등을 통한 무분별한 가입을 막기 위한 메서드.
            throw new CustomException(ErrorCode.INVALID_CODE);
        }

        redisUtil.del(key); // 인증번호 일치할 시, redis에 저장된 값 삭제

        signUpDto.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        Member member = signUpDto.toMemberEntity();

        memberRepository.save(member);

        if (!signUpDto.getSubNewsNames().isEmpty()) {
            for (String subNews: signUpDto.getSubNewsNames()) {

                if (subNews.equals(YU_NEWS_NAME))   // 키워드와 함꼐
                    subNewsService.subscribeToNewsWithKeyword(member, subNews, signUpDto.getKeywords());
                else
                    subNewsService.subscribeToNews(member, subNews);
            }
        }
    }

    /* 멤버 정보 불러오기 */
    @Transactional(readOnly = true)
    public MemberResponseDto readMember(Long memberId) {

        Member findMember = memberRepository.findById(memberId).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<String> keywords = subNewsService.getSubscribedKeyword(findMember.getId());

        return new MemberResponseDto(findMember, keywords);
    }

    /* 멤버 정보 업데이트 */
    @Transactional
    public void updateMember(MemberUpdateDto memberUpdateDto, Long memberId) {

        Member findMember = memberRepository.findById(memberId).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(memberUpdateDto.getCurrentPassword(), findMember.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }

        findMember.updateInfo(passwordEncoder.encode(memberUpdateDto.getNewPassword()), memberUpdateDto.getNickname());
    }

    /* 멤버 삭제하기 */
    @Transactional
    public void deleteMember(Long memberId) {

        associatedEntityService.deleteAllByMemberId(memberId);
        stibeeService.deleteSubscriber(memberRepository.findEmailByMemberId(memberId));  //  Stibee 구독 삭제
        memberRepository.deleteById(memberId);
    }

    /* 특정 소식 구독한 사용자 가져오기 */
    @Transactional(readOnly = true)
    public List<String> findEmailsBySubscribedNews(String newsName) {

        return memberRepository.findEmailsByNewsName(true, newsName);
    }

    /* 특정 소식 구독한 사용자 가져오기 - 키워드 기반 */
    @Transactional(readOnly = true)
    public List<String> findEmailsBySubscribedNewsKeyword(String keyword) {

        return memberRepository.findEmailsByNewsKeyword(true, keyword);
    }

    /* 일간 소식 알림 구독자 가져오기 */
    @Transactional(readOnly = true)
    public List<String> findEmailsByDailySubscribedNews(String newsName) {

        return memberRepository.findEmailsByNewsNameWithDailySubStatus(true, newsName);
    }

    /* 정보 수신 상태 변경 */
    @Transactional
    public void updateSubStatus(SubscribeUpdateDto subscribeUpdateDto) {

        memberRepository.updateSubStatusByUsername(
                subscribeUpdateDto.getUsername(),
                subscribeUpdateDto.isSubStatus(),
                subscribeUpdateDto.isDailySubStatus()
        );
    }

    /* 아이디 중복 확인 */
    public boolean existsUsernameCheck(String username) {

        if (!username.matches(USERNAME_PATTERN)) {
            throw new CustomException(ErrorCode.INVALID_USERNAME_PATTERN);
        }
        return memberRepository.existsByUsername(username);
    }

    /* 닉네임 중복 확인 */
    public boolean existsNicknameCheck(String nickname) {

        if (!nickname.matches(NICKNAME_PATTERN)) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME_PATTERN);
        }
        return memberRepository.existsByNickname(nickname);
    }
}
