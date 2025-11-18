package com.healthcare.web;

import com.healthcare.domain.*;
import com.healthcare.dto.community.CommunityDtos;
import com.healthcare.repository.*;
import com.healthcare.security.SecurityUtil;
import com.healthcare.service.CommunityService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService service;
    private final PostMediaRepository mediaRepo;
    private final PostHashtagRepository postTagRepo;
    private final HashtagRepository tagRepo;
    private final PostRepository postRepo;
    private final SecurityUtil securityUtil;
    private final AccountRepository accountRepo;
    private final UserProfileRepository userProfileRepo;
    private final TrainerProfileRepository trainerProfileRepo;

    public CommunityController(CommunityService service,
                               PostMediaRepository mediaRepo,
                               PostHashtagRepository postTagRepo,
                               HashtagRepository tagRepo,
                               PostRepository postRepo,
                               SecurityUtil securityUtil,
                               AccountRepository accountRepo,
                               UserProfileRepository userProfileRepo,
                               TrainerProfileRepository trainerProfileRepo) {
        this.service = service;
        this.mediaRepo = mediaRepo;
        this.postTagRepo = postTagRepo;
        this.tagRepo = tagRepo;
        this.postRepo = postRepo;
        this.securityUtil = securityUtil;
        this.accountRepo = accountRepo;
        this.userProfileRepo = userProfileRepo;
        this.trainerProfileRepo = trainerProfileRepo;
    }

    /** ---- Post ---- */

    // 게시글 생성: authorAcc를 토큰에서 기본 설정
    @PostMapping("/posts")
    public ResponseEntity<Long> createPost(@RequestBody CommunityDtos.PostCreateRequest req) {
        if (req.authorAcc == null) {
            req.authorAcc = securityUtil.currentAccountId();
        }
        var p = service.createPost(req);
        return ResponseEntity.ok(p.getId());
    }

    @PatchMapping("/posts/{postId}")
    public ResponseEntity<Long> updatePost(@PathVariable Long postId,
                                           @RequestBody CommunityDtos.PostUpdateRequest req) {
        var p = service.updatePost(postId, req);
        return ResponseEntity.ok(p.getId());
    }

    // 목록: sort = latest | likes | views
    @GetMapping("/posts")
    public ResponseEntity<CommunityDtos.PageResponse<CommunityDtos.PostView>> pagePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String hashtag,
            @RequestParam(required = false) Long authorAcc,
            @RequestParam(defaultValue = "latest") String sort
    ) {

        Page<Post> pg = service.pagePosts(page, size, hashtag, authorAcc, sort);

        var posts = pg.getContent();
        var accountIds = posts.stream()
                .map(Post::getAuthorAcc)
                .distinct()
                .toList();
        var accounts = accountRepo.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, acc -> acc));
        var userProfileCache = new HashMap<Long, Long>();
        var trainerProfileCache = new HashMap<Long, Long>();

        var list = new ArrayList<CommunityDtos.PostView>();
        for (var p : posts) {
            var medias = mediaRepo.findByPostId(p.getId()).stream().map(m ->
                    new CommunityDtos.MediaView(
                            m.getId(),
                            m.getMediaType().name(),
                            m.getPublicId(),
                            m.getUrl(),
                            m.getWidth(),
                            m.getHeight(),
                            m.getBytes()
                    )
            ).toList();

            var tags = postTagRepo.findByPostId(p.getId()).stream()
                    .map(ph -> tagRepo.findById(ph.getHashtagId()).map(Hashtag::getTag).orElse(null))
                    .filter(t -> t != null).toList();

            long likeCount = service.likeCount(p.getId());
            long viewCount = service.viewCount(p.getId());

            var account = accounts.get(p.getAuthorAcc());
            var authorInfo = resolveAuthorInfo(account, userProfileCache, trainerProfileCache);

            list.add(new CommunityDtos.PostView(
                    p.getId(),
                    p.getAuthorAcc(),
                    authorInfo.username(),
                    authorInfo.displayName(),
                    authorInfo.role(),
                    authorInfo.profileId(),
                    p.getTitle(),
                    p.getContent(),
                    p.isHidden(),
                    likeCount,
                    viewCount,
                    medias,
                    tags,
                    p.getCreatedAt(),
                    p.getUpdatedAt()
            ));
        }

        return ResponseEntity.ok(new CommunityDtos.PageResponse<>(
                list, pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages(), pg.isLast()
        ));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommunityDtos.PostView> getPost(@PathVariable Long postId) {
        var p = postRepo.findById(postId).orElseThrow();

        var medias = mediaRepo.findByPostId(postId).stream().map(m ->
                new CommunityDtos.MediaView(
                        m.getId(),
                        m.getMediaType().name(),
                        m.getPublicId(),
                        m.getUrl(),
                        m.getWidth(),
                        m.getHeight(),
                        m.getBytes()
                )
        ).toList();

        var tags = postTagRepo.findByPostId(postId).stream()
                .map(ph -> tagRepo.findById(ph.getHashtagId()).map(Hashtag::getTag).orElse(null))
                .filter(t -> t != null).toList();

        long likeCount = service.likeCount(postId);
        long viewCount = service.viewCount(postId);

        var account = p.getAuthorAcc() != null
                ? accountRepo.findById(p.getAuthorAcc()).orElse(null)
                : null;
        var authorInfo = resolveAuthorInfo(
                account,
                new HashMap<>(),
                new HashMap<>()
        );

        return ResponseEntity.ok(new CommunityDtos.PostView(
                p.getId(),
                p.getAuthorAcc(),
                authorInfo.username(),
                authorInfo.displayName(),
                authorInfo.role(),
                authorInfo.profileId(),
                p.getTitle(),
                p.getContent(),
                p.isHidden(),
                likeCount,
                viewCount,
                medias,
                tags,
                p.getCreatedAt(),
                p.getUpdatedAt()
        ));
    }

    /** 유니크 조회수 (계정+일 1회): 토큰에서 accountId */
    @PostMapping("/posts/{postId}/view")
    public ResponseEntity<Void> registerView(@PathVariable Long postId) {
        Long accountId = securityUtil.currentAccountId();
        service.registerView(postId, accountId);
        return ResponseEntity.noContent().build();
    }

    /** 좋아요 토글: 토큰에서 accountId */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long postId) {
        Long accountId = securityUtil.currentAccountId();
        boolean liked = service.toggleLike(postId, accountId);
        return ResponseEntity.ok(liked ? "LIKED" : "UNLIKED");
    }

    /** 숨김/해제 (모더레이션) */
    @PostMapping("/posts/{postId}/hide")
    public ResponseEntity<Long> hidePost(@PathVariable Long postId,
                                         @RequestBody CommunityDtos.HideRequest req) {
        var p = service.hidePost(postId, req.hidden, req.reason);
        return ResponseEntity.ok(p.getId());
    }

    /** ---- Comment ---- */

    // 댓글 작성: authorAcc를 토큰에서 기본 설정
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Long> addComment(@PathVariable Long postId,
                                           @RequestBody CommunityDtos.CommentCreateRequest req) {
        if (req.authorAcc == null) {
            req.authorAcc = securityUtil.currentAccountId();
        }
        var c = service.addComment(postId, req.authorAcc, req.content);
        return ResponseEntity.ok(c.getId());
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommunityDtos.PageResponse<CommunityDtos.CommentView>> pageComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        var pg = service.pageComments(postId, page, size);
        var accountIds = pg.getContent().stream().map(Comment::getAuthorAcc).distinct().toList();
        var accounts = accountRepo.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, acc -> acc));

        var list = pg.getContent().stream()
                .map(c -> {
                    var account = accounts.get(c.getAuthorAcc());
                    String username = null;
                    String displayName = null;
                    String role = null;
                    Long profileId = null;
                    if (account != null) {
                        username = account.getUsername();
                        displayName = account.getDisplayName();
                        role = account.getRole() != null ? account.getRole().name() : null;

                        if (account.getRole() == Account.Role.USER) {
                            profileId = userProfileRepo.findByAccountId(account.getId())
                                    .map(UserProfile::getId)
                                    .orElse(null);
                        } else if (account.getRole() == Account.Role.TRAINER) {
                            profileId = trainerProfileRepo.findByAccountId(account.getId())
                                    .map(TrainerProfile::getId)
                                    .orElse(null);
                        }
                    }
                    return new CommunityDtos.CommentView(
                            c.getId(),
                            c.getAuthorAcc(),
                            username,
                            displayName,
                            role,
                            profileId,
                            c.getContent(),
                            c.isHidden(),
                            c.getCreatedAt()
                    );
                })
                .toList();

        return ResponseEntity.ok(new CommunityDtos.PageResponse<>(
                list, pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages(), pg.isLast()
        ));
    }

    @PostMapping("/comments/{commentId}/hide")
    public ResponseEntity<Long> hideComment(@PathVariable Long commentId,
                                            @RequestBody CommunityDtos.HideRequest req) {
        var c = service.hideComment(commentId, req.hidden, req.reason);
        return ResponseEntity.ok(c.getId());
    }

    /** ---- Report ---- */

    // 신고 작성: reporterAcc가 없으면 토큰에서 기본 설정
    @PostMapping("/reports")
    public ResponseEntity<Long> createReport(@RequestBody CommunityDtos.ReportCreateRequest req) {
        if (req.reporterAcc == null) {
            req.reporterAcc = securityUtil.currentAccountId();
        }
        var r = service.createReport(req);
        return ResponseEntity.ok(r.getId());
    }

    private AuthorInfo resolveAuthorInfo(Account account,
                                         Map<Long, Long> userProfileCache,
                                         Map<Long, Long> trainerProfileCache) {
        if (account == null) {
            return new AuthorInfo(null, null, null, null);
        }
        String username = account.getUsername();
        String displayName = account.getDisplayName();
        String role = null;
        Long profileId = null;
        var accountRole = account.getRole();
        if (accountRole != null) {
            role = accountRole.name();
            Long accountId = account.getId();
            if (accountRole == Account.Role.USER) {
                profileId = userProfileCache.computeIfAbsent(accountId,
                        id -> userProfileRepo.findByAccountId(id).map(UserProfile::getId).orElse(null));
            } else if (accountRole == Account.Role.TRAINER) {
                profileId = trainerProfileCache.computeIfAbsent(accountId,
                        id -> trainerProfileRepo.findByAccountId(id).map(TrainerProfile::getId).orElse(null));
            }
        }
        return new AuthorInfo(username, displayName, role, profileId);
    }

    private record AuthorInfo(String username, String displayName, String role, Long profileId) {
    }
}
