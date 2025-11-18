package com.healthcare.service;

import com.healthcare.domain.*;
import com.healthcare.dto.community.CommunityDtos;
import com.healthcare.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CommunityService {

    private final PostRepository postRepo;
    private final PostMediaRepository mediaRepo;
    private final HashtagRepository tagRepo;
    private final PostHashtagRepository postTagRepo;
    private final PostLikeRepository likeRepo;
    private final PostViewRepository viewRepo;
    private final CommentRepository commentRepo;
    private final ReportRepository reportRepo;

    public CommunityService(PostRepository postRepo,
                            PostMediaRepository mediaRepo,
                            HashtagRepository tagRepo,
                            PostHashtagRepository postTagRepo,
                            PostLikeRepository likeRepo,
                            PostViewRepository viewRepo,
                            CommentRepository commentRepo,
                            ReportRepository reportRepo) {
        this.postRepo = postRepo;
        this.mediaRepo = mediaRepo;
        this.tagRepo = tagRepo;
        this.postTagRepo = postTagRepo;
        this.likeRepo = likeRepo;
        this.viewRepo = viewRepo;
        this.commentRepo = commentRepo;
        this.reportRepo = reportRepo;
    }

    /* ---------- Post ---------- */

    @Transactional
    public Post createPost(CommunityDtos.PostCreateRequest req) {
        if (req.title == null || req.title.isBlank()) throw new IllegalArgumentException("title is blank");
        if (req.content == null || req.content.isBlank()) throw new IllegalArgumentException("content is blank");

        var p = new Post();
        p.setAuthorAcc(req.authorAcc);
        p.setTitle(req.title);
        p.setContent(req.content);
        var saved = postRepo.save(p);

        // media
        if (req.mediaList != null) {
            for (var m : req.mediaList) {
                var pm = new PostMedia();
                pm.setPostId(saved.getId());
                pm.setMediaType(PostMedia.MediaType.valueOf(m.mediaType.toUpperCase()));
                pm.setPublicId(m.publicId);
                pm.setUrl(m.url);
                pm.setWidth(m.width);
                pm.setHeight(m.height);
                pm.setBytes(m.bytes);
                mediaRepo.save(pm);
            }
        }
        // hashtags
        if (req.hashtags != null) attachHashtags(saved.getId(), req.hashtags);
        return saved;
    }

    @Transactional
    public Post updatePost(Long postId, CommunityDtos.PostUpdateRequest req) {
        var p = postRepo.findById(postId).orElseThrow();
        if (req.title != null) p.setTitle(req.title);
        if (req.content != null) p.setContent(req.content);

        // media 전체 교체 정책
        if (req.mediaList != null) {
            var list = mediaRepo.findByPostId(postId);
            mediaRepo.deleteAll(list);
            for (var m : req.mediaList) {
                var pm = new PostMedia();
                pm.setPostId(postId);
                pm.setMediaType(PostMedia.MediaType.valueOf(m.mediaType.toUpperCase()));
                pm.setPublicId(m.publicId);
                pm.setUrl(m.url);
                pm.setWidth(m.width);
                pm.setHeight(m.height);
                pm.setBytes(m.bytes);
                mediaRepo.save(pm);
            }
        }
        // hashtags 교체
        if (req.hashtags != null) {
            var cur = postTagRepo.findByPostId(postId);
            postTagRepo.deleteAll(cur);
            attachHashtags(postId, req.hashtags);
        }
        return p;
    }

    private void attachHashtags(Long postId, List<String> tags) {
        for (String raw : tags) {
            if (raw == null || raw.isBlank()) continue;

            String trimmed = raw.trim();
            final String tagVal = trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;

            var ht = tagRepo.findByTagIgnoreCase(tagVal).orElseGet(() -> {
                var h = new Hashtag();
                h.setTag(tagVal);
                return tagRepo.save(h);
            });

            var ph = new PostHashtag();
            ph.setPostId(postId);
            ph.setHashtagId(ht.getId());
            postTagRepo.save(ph);
        }
    }

    /** 목록 페이징 + 정렬(최신/좋아요/조회) + 해시태그/작성자 필터 */
    @Transactional(readOnly = true)
    public Page<Post> pagePosts(Integer page, Integer size, String hashtag, Long authorAcc, String sort) {
        var pageable = PageRequest.of(page, size);

        // 작성자 필터 우선
        if (authorAcc != null) {
            return postRepo.findByAuthorAccAndHiddenFalse(authorAcc, pageable);
        }

        // 해시태그 + 정렬
        if (hashtag != null && !hashtag.isBlank()) {
            String tag = hashtag.trim();
            String key = (sort == null ? "latest" : sort.toLowerCase());
            return switch (key) {
                case "likes" -> postRepo.findByHashtagPopularByLikes(tag, pageable);
                case "views" -> postRepo.findByHashtagPopularByViews(tag, pageable);
                default -> postRepo.findByHashtag(tag, pageable); // latest
            };
        }

        // 전체 + 정렬
        String key = (sort == null ? "latest" : sort.toLowerCase());
        return switch (key) {
            case "likes" -> postRepo.findPopularByLikes(pageable);
            case "views" -> postRepo.findPopularByViews(pageable);
            default -> postRepo.findByHiddenFalse(pageable); // latest
        };
    }

    @Transactional(readOnly = true)
    public long likeCount(Long postId) { return likeRepo.countByPostId(postId); }

    @Transactional(readOnly = true)
    public long viewCount(Long postId) { return viewRepo.countByPostId(postId); }

    @Transactional
    public void registerView(Long postId, Long accountId) {
        var today = LocalDate.now(); // 서버 TZ 기준
        boolean exists = viewRepo.existsByPostIdAndAccountIdAndViewDate(postId, accountId, today);
        if (!exists) {
            var pv = new PostView();
            pv.setPostId(postId);
            pv.setAccountId(accountId);
            pv.setViewDate(today);
            pv.setCnt(1);
            viewRepo.save(pv);
        }
    }

    @Transactional
    public boolean toggleLike(Long postId, Long accountId) {
        var pk = new PostLike.PK(postId, accountId);
        boolean exists = likeRepo.existsByPostIdAndAccountId(postId, accountId);
        if (exists) {
            likeRepo.deleteById(pk);
            return false; // unliked
        } else {
            var pl = new PostLike();
            pl.setPostId(postId);
            pl.setAccountId(accountId);
            likeRepo.save(pl);
            return true; // liked
        }
    }

    @Transactional
    public Post hidePost(Long postId, boolean hidden, String reason) {
        var p = postRepo.findById(postId).orElseThrow();
        p.setHidden(hidden);
        p.setHiddenReason(hidden ? reason : null);
        return p;
    }

    /* ---------- Comment ---------- */

    @Transactional
    public Comment addComment(Long postId, Long authorAcc, String content) {
        var c = new Comment();
        c.setPostId(postId);
        c.setAuthorAcc(authorAcc);
        c.setContent(content);
        return commentRepo.save(c);
    }

    @Transactional(readOnly = true)
    public Page<Comment> pageComments(Long postId, int page, int size) {
        return commentRepo.findByPostIdAndHiddenFalseOrderByCreatedAtAsc(postId, PageRequest.of(page, size));
    }

    @Transactional
    public Comment hideComment(Long commentId, boolean hidden, String reason) {
        var c = commentRepo.findById(commentId).orElseThrow();
        c.setHidden(hidden);
        c.setHiddenReason(hidden ? reason : null);
        return c;
    }

    /* ---------- Report ---------- */

    @Transactional
    public Report createReport(CommunityDtos.ReportCreateRequest req) {
        var r = new Report();
        r.setTargetType(Report.TargetType.valueOf(req.targetType.toUpperCase()));
        r.setTargetId(req.targetId);
        r.setReporterAcc(req.reporterAcc);
        r.setReason(req.reason);
        return reportRepo.save(r);
    }
}
