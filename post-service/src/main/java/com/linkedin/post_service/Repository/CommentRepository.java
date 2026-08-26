package com.linkedin.post_service.Repository;

import com.linkedin.post_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,String> {
}
