package project.yourNews.domains.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.yourNews.common.aop.annotation.VerifyAuthentication;
import project.yourNews.common.exception.CustomException;
import project.yourNews.common.exception.error.ErrorCode;
import project.yourNews.common.utils.page.RestPage;
import project.yourNews.domains.category.domain.Category;
import project.yourNews.domains.category.repository.CategoryRepository;
import project.yourNews.domains.common.service.AssociatedEntityService;
import project.yourNews.domains.member.domain.Member;
import project.yourNews.domains.member.repository.MemberRepository;
import project.yourNews.domains.post.domain.Post;
import project.yourNews.domains.post.dto.PostInfoDto;
import project.yourNews.domains.post.dto.PostRequestDto;
import project.yourNews.domains.post.dto.PostResponseDto;
import project.yourNews.domains.post.repository.PostRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final AssociatedEntityService associatedEntityService;

    /* 게시글 저장 */
    @Transactional
    @CacheEvict(value = "noticePosts", allEntries = true, condition = "#categoryName.equals('notice')")
    public Long savePost(PostRequestDto postDto, String username, String categoryName) {

        Member findMember = memberRepository.findByUsername(username).orElseThrow(() ->
                new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Category findCategory = categoryRepository.findByName(categoryName).orElseThrow(() ->
                new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        Post createdPost = postRepository.save(
                postDto.toPostEntity(
                        findMember,
                        findCategory
                )
        );
        return createdPost.getId();
    }

    /* 게시글 불러오기 */
    @Transactional(readOnly = true)
    public PostResponseDto readPost(Long postId) {

        Post findPost = postRepository.findById(postId).orElseThrow(() ->
                new CustomException(ErrorCode.POST_NOT_FOUND));

        return new PostResponseDto(findPost);
    }

    /* 카테고리 게시글 전체 들고오기 */
    @Transactional(readOnly = true)
    @Cacheable(value = "noticePosts", key = "#categoryName + ':' + #pageable.pageNumber", condition = "#categoryName.equals('notice')")
    public RestPage<PostInfoDto> readPostsByCategory(String categoryName, Pageable pageable) {

        Page<Post> posts = postRepository.findByCategory_Name(categoryName, pageable);

        return new RestPage<>(posts.map(PostInfoDto::new));
    }

    /* 게시글 업데이트 */
    @Transactional
    @VerifyAuthentication
    @CacheEvict(value = "noticePosts", allEntries = true, condition = "#categoryName.equals('notice')")
    public void updatePost(PostRequestDto postDto, Long postId, String categoryName) {

        Post findPost = postRepository.findById(postId).orElseThrow(() ->
                new CustomException(ErrorCode.POST_NOT_FOUND));

        findPost.updatePost(postDto.getTitle(), postDto.getContent());
    }

    /* 게시글 삭제하기 */
    @Transactional
    @VerifyAuthentication
    public void deletePost(Long postId) {

        associatedEntityService.deleteAllByPostId(postId);  // 좋아요 연관관계 삭제
        postRepository.deleteById(postId);
    }
}
