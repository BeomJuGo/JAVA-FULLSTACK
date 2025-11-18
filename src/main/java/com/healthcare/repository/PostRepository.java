package com.healthcare.repository;

import com.healthcare.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** 최신순 기본 목록 (hidden 제외) */
    Page<Post> findByHiddenFalse(Pageable pageable);

    /** 작성자 필터 (hidden 제외) */
    Page<Post> findByAuthorAccAndHiddenFalse(Long authorAcc, Pageable pageable);

    /** 해시태그 필터 + 최신순 */
    @Query("""
           select p from Post p
           where p.hidden = false
             and p.id in (
               select ph.postId from PostHashtag ph
               join Hashtag h on h.id = ph.hashtagId
               where lower(h.tag) = lower(:tag)
             )
           order by p.createdAt desc
           """)
    Page<Post> findByHashtag(@Param("tag") String tag, Pageable pageable);

    /** 전체 인기글(좋아요수 내림차순 → 최신순 tie-break) */
    @Query("""
           select p from Post p
           where p.hidden = false
           order by
             (select count(pl) from PostLike pl where pl.postId = p.id) desc,
             p.createdAt desc
           """)
    Page<Post> findPopularByLikes(Pageable pageable);

    /** 전체 인기글(조회수 내림차순 → 최신순 tie-break) */
    @Query("""
           select p from Post p
           where p.hidden = false
           order by
             (select coalesce(sum(pv.cnt), 0) from PostView pv where pv.postId = p.id) desc,
             p.createdAt desc
           """)
    Page<Post> findPopularByViews(Pageable pageable);

    /** 해시태그 인기글(좋아요수) */
    @Query("""
           select p from Post p
           where p.hidden = false
             and p.id in (
               select ph.postId from PostHashtag ph
               join Hashtag h on h.id = ph.hashtagId
               where lower(h.tag) = lower(:tag)
             )
           order by
             (select count(pl) from PostLike pl where pl.postId = p.id) desc,
             p.createdAt desc
           """)
    Page<Post> findByHashtagPopularByLikes(@Param("tag") String tag, Pageable pageable);

    /** 해시태그 인기글(조회수) */
    @Query("""
           select p from Post p
           where p.hidden = false
             and p.id in (
               select ph.postId from PostHashtag ph
               join Hashtag h on h.id = ph.hashtagId
               where lower(h.tag) = lower(:tag)
             )
           order by
             (select coalesce(sum(pv.cnt), 0) from PostView pv where pv.postId = p.id) desc,
             p.createdAt desc
           """)
    Page<Post> findByHashtagPopularByViews(@Param("tag") String tag, Pageable pageable);
}
