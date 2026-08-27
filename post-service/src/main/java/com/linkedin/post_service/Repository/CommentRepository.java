package com.linkedin.post_service.Repository;

import com.linkedin.post_service.entity.Comment;
import com.linkedin.post_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,String> {
    Optional<List<Comment>> findByPostIdOrderByCreatedAtDesc(String postId);
}
